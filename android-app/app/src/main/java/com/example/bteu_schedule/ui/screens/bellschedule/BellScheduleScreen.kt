package com.example.bteu_schedule.ui.screens.bellschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.BellScheduleUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.viewmodel.BellScheduleUiState
import com.example.bteu_schedule.ui.viewmodel.BellScheduleViewModel

@Composable
fun BellScheduleScreen(
    modifier: Modifier = Modifier,
    group: GroupUi? = null,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: BellScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBellSchedule()
    }

    val colors = designColors()
    
    // Полноэкранный режим: фон идет на весь экран
    // Градиент хедера плавно переходит в контент (как на других экранах)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryGradientStart,
                        colors.primaryGradientMid,
                        colors.primaryGradientEnd,
                        colors.bg
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с единым градиентом
            AdaptiveHeader(
                title = "Расписание звонков",
                subtitle = "Время начала и окончания пар",
                isVisible = true,
                isScrolled = false,
                backButton = showBackButton,
                onBackClick = if (showBackButton) onBack else null,
                headerType = if (showBackButton) HeaderType.NAVIGATION else HeaderType.SECONDARY,
                avatarIcon = if (!showBackButton) Icons.Default.Person else null,
                onAvatarClick = if (!showBackButton) onProfileClick else null
            )
            
            // Контент расписания звонков
            BellScheduleContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState
            )
        }
    }
}

@Composable
private fun BellScheduleContent(
    modifier: Modifier = Modifier,
    uiState: BellScheduleUiState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is BellScheduleUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is BellScheduleUiState.Success -> {
                if (uiState.schedule.isEmpty()) {
                    EmptyBellScheduleCard()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(DesignSpacing.Base),
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                    ) {
                        items(uiState.schedule) { bell ->
                            BellScheduleCard(bell = bell)
                        }
                    }
                }
            }
            is BellScheduleUiState.Error -> {
                ErrorCard(
                    message = uiState.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is BellScheduleUiState.Empty -> {
                EmptyBellScheduleCard()
            }
        }
    }
}

@Composable
private fun BellScheduleCard(bell: BellScheduleUi) {
    val colors = designColors()
    val isDarkTheme = MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG
    
    // Design Tokens: Светлые карточки с границей и тенью
    val cardBackground = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Surface
    } else {
        MaterialTheme.colorScheme.surface // --surface-1: #FFFFFF
    }
    val borderColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Border
    } else {
        Color(0x0F0D1333) // --border: rgba(13,19,51,0.06)
    }
    val textColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextPrimary
    } else {
        Color(0xFF0D1333) // --text-primary: #0D1333
    }
    val textSecondaryColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextSecondary
    } else {
        Color(0xFF6B7280) // --text-secondary: #6B7280
    }
    
    val cardShape = RoundedCornerShape(DesignRadius.M) // Design Tokens: 16dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .applyShadow(
                shadowSpec = DesignShadows.Low, // Design Tokens: тень 4dp
                shape = cardShape
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = cardShape
            ),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Тень через applyShadow
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSpacing.Base), // Design Tokens: 16dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер пары с иконкой
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base), // Design Tokens: 16dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignIconSizes.IconButtonSize) // Design Tokens: 48dp
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.primaryGradientStart,  // #3A4DFF
                                    colors.primaryGradientEnd     // #000064
                                )
                            ),
                            shape = RoundedCornerShape(DesignRadius.XS) // Design Tokens: 8dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${bell.lessonNumber}",
                        style = MaterialTheme.typography.headlineSmall, // Design Tokens: 18sp/24px 600
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column {
                    Text(
                        text = bell.description ?: "Пара ${bell.lessonNumber}",
                        style = MaterialTheme.typography.titleMedium, // Design Tokens: 16sp/22px Medium
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    if (bell.lessonStart != null && bell.lessonEnd != null) {
                        // Формируем время для отображения
                        // Для каждой пары показываем оба времени (например, 9:00-9:45, 9:50-10:35)
                        val timeText = getBellScheduleTimeText(bell.lessonNumber, bell.lessonStart, bell.lessonEnd)
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodyLarge, // Design Tokens: 16sp/22px 400
                            color = colors.primaryLight, // Primary цвет для времени
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Информация о перерывах
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
            ) {
                if (bell.breakTimeMinutes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = colors.primaryLight, // Primary цвет для иконок
                            modifier = Modifier.size(DesignIconSizes.Small) // Design Tokens: 20dp
                        )
                        Text(
                            text = "Перерыв: ${bell.breakTimeMinutes} мин",
                            style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                            color = textSecondaryColor
                        )
                    }
                }
                if (bell.breakAfterLessonMinutes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.primaryLight, // Primary цвет для иконок
                            modifier = Modifier.size(DesignIconSizes.Small) // Design Tokens: 20dp
                        )
                        Text(
                            text = "После пары: ${bell.breakAfterLessonMinutes} мин",
                            style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBellScheduleCard() {
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
                // Иконка расписания звонков
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(DesignIconSizes.Large * 2), // Design Tokens: 64dp для empty state
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Мягкий нейтральный цвет
                )
                
                Text(
                    text = "РАСПИСАНИЕ ЗВОНКОВ НЕ НАЙДЕНО",
                    style = MaterialTheme.typography.headlineMedium, // Design Tokens: --type-h2: 20px/28px 600
                    fontWeight = FontWeight.SemiBold, // 600
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Расписание звонков не загружено",
                    style = MaterialTheme.typography.bodyMedium, // Design Tokens: --type-small: 13px/18px 400
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Формирует текст времени для расписания звонков
 * Показывает оба времени для каждой пары (например, "9:00-9:45, 9:50-10:35")
 */
@Composable
private fun getBellScheduleTimeText(lessonNumber: Int, start: String, end: String): String {
    // Статическое расписание звонков БТЭУ
    val schedule = mapOf(
        1 to "9:00-9:45, 9:50-10:35",
        2 to "10:50-11:35, 11:40-12:25",
        3 to "12:55-13:40, 13:45-14:30",
        4 to "14:40-15:25, 15:30-16:15",
        5 to "16:25-17:10, 17:15-18:00",
        6 to "18:30-19:15, 19:20-20:05",
        7 to "20:15-21:00, 21:05-21:50"
    )
    
    return schedule[lessonNumber] ?: "$start - $end"
}

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    // Danger colors - правильные красные цвета для ошибок
    val dangerBackground = Color(0xFFFFE5E5) // #FFE5E5 - фон
    val dangerText = Color(0xFFFF3B30) // #FF3B30 - текст и иконка
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSpacing.Base),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignRadius.L), // 24dp для больших карточек
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = dangerBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSpacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = dangerText,
                    modifier = Modifier.size(DesignIconSizes.Large * 2)
                )
                Text(
                    text = "Ошибка загрузки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dangerText
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = dangerText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

