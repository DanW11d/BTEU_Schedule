package com.example.bteu_schedule.ui.screens.schedule

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import com.example.bteu_schedule.ui.theme.DesignHeights
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.ui.components.LessonCard
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import com.example.bteu_schedule.ui.utils.premiumCardPressAnimation
import com.example.bteu_schedule.ui.viewmodel.ScheduleUiState
import com.example.bteu_schedule.ui.viewmodel.ScheduleViewModel
import com.example.bteu_schedule.utils.WeekCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    group: GroupUi? = null,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentGroup = remember(group) {
        group ?: GroupUi("S-4", "Инженеры-программисты", 4, "", "FKIF", "full_time", null)
    }

    var isOddWeek by rememberSaveable { mutableStateOf(WeekCalculator.isCurrentWeekOdd()) }
    var selectedDayIndex by rememberSaveable { mutableStateOf(0) }

    // Загружаем расписание при изменении группы или параметров
    LaunchedEffect(selectedDayIndex, isOddWeek, currentGroup.code, group?.code) {
        val groupCode = group?.code ?: currentGroup.code
        if (groupCode.isNotBlank()) {
            viewModel.loadSchedule(groupCode, selectedDayIndex + 1, isOddWeek)
        }
    }

    val colors = designColors()
    
    // ✅ Используем MaterialTheme.colorScheme.background вместо градиента для единообразия
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с единым градиентом - как на главном экране
            AdaptiveHeader(
                title = "Расписание занятий",
                subtitle = null,
                isVisible = true,
                isScrolled = false,
                backButton = showBackButton,
                onBackClick = if (showBackButton) onBack else null,
                headerType = if (showBackButton) HeaderType.NAVIGATION else HeaderType.SECONDARY,
                avatarIcon = if (!showBackButton) Icons.Default.Person else null,
                onAvatarClick = if (!showBackButton) onProfileClick else null
            )
            
            // DaySelector на градиентном фоне, сразу после хедера
            DaySelector(
                selectedIndex = selectedDayIndex,
                onDaySelected = { selectedDayIndex = it }
            )
            
            // Контент расписания
            SwipeableScheduleContent(
                selectedDayIndex = selectedDayIndex,
                onDayIndexChanged = { selectedDayIndex = it },
                currentGroup = currentGroup,
                isOddWeek = isOddWeek,
                onWeekChange = { isOddWeek = it },
                uiState = uiState
            )
        }
    }
}

/**
 * Обертка для контента расписания с поддержкой жестов свайпа
 */
@Composable
private fun SwipeableScheduleContent(
    selectedDayIndex: Int,
    onDayIndexChanged: (Int) -> Unit,
    currentGroup: GroupUi,
    isOddWeek: Boolean,
    onWeekChange: (Boolean) -> Unit,
    uiState: ScheduleUiState
) {
    var totalDragAmount by remember { mutableStateOf(0f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        totalDragAmount = 0f
                    },
                    onDragEnd = {
                        // После завершения жеста проверяем, нужно ли переключить день
                        if (kotlin.math.abs(totalDragAmount) > 150f) {
                            if (totalDragAmount > 0) {
                                // Свайп влево - следующий день
                                if (selectedDayIndex < 5) {
                                    onDayIndexChanged(selectedDayIndex + 1)
                                }
                            } else {
                                // Свайп вправо - предыдущий день
                                if (selectedDayIndex > 0) {
                                    onDayIndexChanged(selectedDayIndex - 1)
                                }
                            }
                        }
                        totalDragAmount = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // Накапливаем общее расстояние свайпа
                        totalDragAmount += dragAmount
                    }
                )
            }
    ) {
        // Контент расписания (DaySelector уже размещен в основном Column)
        ScheduleContent(uiState, selectedIndex = selectedDayIndex)
    }
}

@Composable
private fun DaySelector(
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Оптимизация: кэшируем список дней, так как он не меняется
    val days = remember {
        listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
    }
    val colors = designColors()
    
    // Современный дизайн селектора дней с белым текстом на градиентном фоне
    // Размещается сразу после хедера, на градиентном фоне
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSpacing.Base, vertical = DesignSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedIndex == index
            
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "tab_scale_$index"
            )
            
            // Белый текст для всех дней на градиентном фоне
            val textColor by animateColorAsState(
                targetValue = Color.White,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "tab_color_$index"
            )
            
            // Светлый фон для выделенного дня (белый с прозрачностью)
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    Color.White.copy(alpha = 0.25f) // Полупрозрачный белый фон
                } else {
                    Color.Transparent
                },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "tab_bg_$index"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(DesignRadius.M)) // Более скругленные углы для выделенного дня
                    .background(backgroundColor)
                    .clickable { onDaySelected(index) }
                    .padding(vertical = DesignSpacing.XS, horizontal = DesignSpacing.XS),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ScheduleContent(uiState: ScheduleUiState, selectedIndex: Int) {
    AnimatedContent(
        targetState = selectedIndex,
        transitionSpec = {
            if (targetState > initialState) {
                // Переход вперед (следующий день)
                (fadeIn(animationSpec = tween(durationMillis = 300)) + 
                 slideInHorizontally(
                     initialOffsetX = { it },
                     animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                 )) togetherWith
                (fadeOut(animationSpec = tween(durationMillis = 300)) +
                 slideOutHorizontally(
                     targetOffsetX = { -it },
                     animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                 ))
            } else {
                // Переход назад (предыдущий день)
                (fadeIn(animationSpec = tween(durationMillis = 300)) + 
                 slideInHorizontally(
                     initialOffsetX = { -it },
                     animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                 )) togetherWith
                (fadeOut(animationSpec = tween(durationMillis = 300)) +
                 slideOutHorizontally(
                     targetOffsetX = { it },
                     animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                 ))
            }
        },
        label = "schedule_content"
    ) { dayIndex ->
        when (uiState) {
            is ScheduleUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScheduleUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message)
                }
            }
            is ScheduleUiState.Empty -> EmptyDayCard(selectedIndex = dayIndex)
            is ScheduleUiState.Success -> {
                if (uiState.lessons.isEmpty()) {
                    EmptyDayCard(selectedIndex = dayIndex)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = DesignSpacing.Base,
                            vertical = DesignSpacing.S
                        ),
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                    ) {
                        itemsIndexed(uiState.lessons) { index, lesson ->
                            AnimatedLessonCard(
                                lesson = lesson,
                                displayPairNumber = index + 1,
                                index = index
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка для пустого состояния - "ВЫХОДНОЙ ДЕНЬ"
 */
@Composable
private fun EmptyDayCard(selectedIndex: Int) {
    val daysFull = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
    val currentDay = daysFull[selectedIndex]
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSpacing.Base), // Design Tokens: 16dp боковые поля
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp (--radius-lg)
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Тень через shadow modifier
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSpacing.XXL), // Design Tokens: 32dp внутренний padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp между элементами
            ) {
                // Иконка выходного дня
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp), // Design Tokens: крупная иконка для empty state
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Мягкий нейтральный цвет
                )
                
                Text(
                    text = "ВЫХОДНОЙ ДЕНЬ",
                    style = MaterialTheme.typography.headlineMedium, // Design Tokens: --type-h2: 20px/28px 600
                    fontWeight = FontWeight.SemiBold, // 600
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Сегодня занятий не запланировано",
                    style = MaterialTheme.typography.bodyMedium, // Design Tokens: --type-small: 13px/18px 400
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Text(
                    text = currentDay,
                    style = MaterialTheme.typography.titleMedium, // Design Tokens: 16px/22px Medium
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Премиальная анимированная карточка занятия с staggered анимацией
 */
@Composable
private fun AnimatedLessonCard(
    lesson: LessonUi,
    displayPairNumber: Int,
    index: Int
) {
    AnimatedVisibility(
        visible = true,
        enter = premiumCardAppearAnimation(index),
        exit = fadeOut() + slideOutVertically()
    ) {
        LessonCard(
            lesson = lesson,
            displayPairNumber = displayPairNumber,
            modifier = Modifier.premiumCardPressAnimation(isPressed = false)
        )
    }
}

@Composable
private fun DayHeader(selectedIndex: Int) {
    // Убираем DayHeader, так как дни уже показаны в DaySelector
    // Это убирает дублирование информации
}
