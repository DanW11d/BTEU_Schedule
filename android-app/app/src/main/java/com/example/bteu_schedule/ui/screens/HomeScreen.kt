package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.ModernGradientCard
import com.example.bteu_schedule.ui.theme.AppGradients
import com.example.bteu_schedule.ui.theme.AppColors
import com.example.bteu_schedule.ui.theme.AppDimensions
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.premiumHeaderScrollAnimation
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import com.example.bteu_schedule.utils.WeekCalculator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Главный экран приложения с современным дизайном
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentGroup: GroupUi? = null,
    onScheduleClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onTestsClick: () -> Unit = {},
    onBellScheduleClick: () -> Unit = {},
    onDepartmentsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Оптимизация: кэшируем дорогие вычисления
    val currentWeekNumber = remember { WeekCalculator.getCurrentWeekNumber() }
    val isOddWeek = remember { WeekCalculator.isCurrentWeekOdd() }
    val weekParity = remember(isOddWeek) {
        if (isOddWeek) "Нечётная" else "Чётная"
    }

    // Форматирование даты - кэшируем форматтер и текущую дату
    val dateFormat = remember {
        SimpleDateFormat("d MMMM, EEEE", Locale("ru", "RU"))
    }
    val currentDate = remember {
        dateFormat.format(Date())
    }

    // Анимация появления экрана - отложенная для оптимизации первого рендера
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Небольшая задержка для оптимизации первого рендера
        kotlinx.coroutines.delay(16) // Один кадр (16ms для 60fps)
        isScreenVisible = true
    }

    // Получаем цвета дизайн-системы
    val colors = designColors()
    
    // Состояние скролла для адаптивного хедера
    val listState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }
    // Анимация хедера (composable функция, вызывается напрямую)
    val headerAnimation = premiumHeaderScrollAnimation(isScrolled.value)
    
    // Полноэкранный режим: фон идет на весь экран, включая под статус-бар и системную панель
    // Используем MaterialTheme.colorScheme.background для правильной работы темы
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Контент без statusBarsPadding - хедер сам включает статус-бар
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с фиксированной высотой и единым градиентом
            com.example.bteu_schedule.ui.components.AdaptiveHeader(
                title = "БТЭУ Расписание",
                subtitle = null, // Убран подзаголовок "Расписание университета"
                isVisible = isScreenVisible,
                isScrolled = isScrolled.value,
                currentGroup = currentGroup,
                onProfileClick = onProfileClick
            )

            // Контент начинается сразу после хедера (без белой полоски)
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            // Высота нижней навигации: используем DesignHeights.BottomNavigation (84dp) + дополнительный отступ
            val bottomNavigationHeight = DesignHeights.BottomNavigation
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base,
                    top = DesignSpacing.M, // ✅ Отступ как в настройках
                    bottom = DesignSpacing.XL + bottomNavigationHeight + navigationBarsPadding.calculateBottomPadding() + DesignSpacing.L
                ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.L)
            ) {
                // Приветственный блок с анимацией
                item {
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    initialOffsetY = { 30 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                        exit = fadeOut()
                    ) {
                        ModernWelcomeCard(
                            currentGroup = currentGroup,
                            currentWeekNumber = currentWeekNumber,
                            weekParity = weekParity,
                            currentDate = currentDate
                        )
                    }
                }

                // Подзаголовок
                item {
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                                slideInVertically(
                                    initialOffsetY = { 20 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Выберите раздел",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = DesignSpacing.XS) // Design Tokens: 4dp
                        )
                    }
                }

                // Карточки с анимацией появления (staggered) - все с новыми синими градиентами
                val cards = listOf(
                    CardData(
                        gradientColors = listOf(
                            colors.classicBlueStart, // Classic Blue
                            colors.classicBlueEnd
                        ),
                        icon = Icons.Default.CalendarToday,
                        title = "Расписание занятий",
                        subtitle = "Посмотреть расписание пар",
                        onClick = onScheduleClick
                    ),
                    CardData(
                        gradientColors = listOf(
                            colors.brightBlueStart, // Bright Blue
                            colors.brightBlueEnd
                        ),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "Расписание экзаменов",
                        subtitle = "Даты и время экзаменов",
                        onClick = onExamsClick
                    ),
                    CardData(
                        gradientColors = listOf(
                            colors.softBlueStart, // Soft Blue
                            colors.softBlueEnd
                        ),
                        icon = Icons.Default.CheckCircle,
                        title = "Расписание зачетов",
                        subtitle = "График зачетной сессии",
                        onClick = onTestsClick
                    ),
                    CardData(
                        gradientColors = listOf(
                            colors.classicBlueStart, // Classic Blue
                            colors.classicBlueEnd
                        ),
                        icon = Icons.Default.Schedule,
                        title = "Расписание звонков",
                        subtitle = "Время начала и окончания пар",
                        onClick = onBellScheduleClick
                    ),
                    CardData(
                        gradientColors = listOf(
                            colors.brightBlueStart, // Bright Blue
                            colors.brightBlueEnd
                        ),
                        icon = Icons.Default.Business,
                        title = "Кафедры",
                        subtitle = "Все кафедры университета",
                        onClick = onDepartmentsClick
                    )
                )

                itemsIndexed(cards) { index, card ->
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = premiumCardAppearAnimation(index),
                        exit = fadeOut()
                    ) {
                        ModernGradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradientColors = card.gradientColors,
                            icon = card.icon,
                            title = card.title,
                            subtitle = card.subtitle,
                            onClick = card.onClick
                        )
                    }
                }
                
                // Дополнительный отступ в конце списка, чтобы последняя карточка не обрезалась
                item {
                    Spacer(modifier = Modifier.height(DesignSpacing.M))
                }
            }
        }
    }
}

@Composable
private fun ModernWelcomeCard(
    currentGroup: GroupUi?,
    currentWeekNumber: Int,
    weekParity: String,
    currentDate: String
) {
    val shape = RoundedCornerShape(24.dp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = DesignSpacing.S, // Design Tokens: 8dp (для shadow elevation)
                    shape = shape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = shape
                )
                .padding(DesignSpacing.XL) // Design Tokens: 24dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp
            ) {
                // Приветствие без иконки
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.S) // Design Tokens: 8dp
                ) {
                    Text(
                        text = "Добро пожаловать!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentGroup != null) {
                        Text(
                            text = currentGroup.name.ifBlank { "Группа: ${currentGroup.code}" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                // Информация о неделе
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
                    ) {
                        Text(
                            text = "Учебная неделя",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Неделя $currentWeekNumber",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = weekParity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
                    ) {
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private data class CardData(
    val gradientColors: List<Color>,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)