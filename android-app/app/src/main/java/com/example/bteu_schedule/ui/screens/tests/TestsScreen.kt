package com.example.bteu_schedule.ui.screens.tests

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
import com.example.bteu_schedule.domain.models.ExamUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.viewmodel.ExamUiState
import com.example.bteu_schedule.ui.viewmodel.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    modifier: Modifier = Modifier,
    group: GroupUi? = null,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: TestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentGroup = remember(group) {
        group ?: GroupUi("S-41", "Инженеры-программисты", 4, "", "FKIF", "full_time", null)
    }

    LaunchedEffect(currentGroup.code) {
        viewModel.loadTests(currentGroup.code)
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
                title = "Расписание зачетов",
                subtitle = null,
                isVisible = true,
                isScrolled = false,
                backButton = showBackButton,
                onBackClick = if (showBackButton) onBack else null,
                headerType = if (showBackButton) HeaderType.NAVIGATION else HeaderType.SECONDARY,
                avatarIcon = if (!showBackButton) Icons.Default.Person else null,
                onAvatarClick = if (!showBackButton) onProfileClick else null
            )
            
            // Контент зачетов
            TestContent(uiState = uiState)
        }
    }
}

@Composable
private fun TestContent(uiState: ExamUiState) {
    when (uiState) {
        is ExamUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ExamUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is ExamUiState.Empty -> {
            EmptyTestsCard()
        }
        is ExamUiState.Success -> {
            if (uiState.exams.isEmpty()) {
                EmptyTestsCard()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(DesignSpacing.Base),
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                ) {
                    items(uiState.exams) { test ->
                        TestCard(test = test)
                    }
                }
            }
        }
    }
}

@Composable
private fun TestCard(test: ExamUi) {
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
    val iconColor = colors.primaryLight // Primary цвет для иконок
    
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
                .padding(DesignSpacing.Base) // Design Tokens: 16dp
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Дата
                Text(
                    text = test.date,
                    style = MaterialTheme.typography.headlineSmall, // Design Tokens: 18sp/24px 600
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(DesignSpacing.M)) // Design Tokens: 12dp
                
                // Предмет
                Text(
                    text = test.subject,
                    style = MaterialTheme.typography.titleMedium, // Design Tokens: 16sp/22px Medium
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(DesignSpacing.M)) // Design Tokens: 12dp
                
                // Преподаватель
                if (test.teacher?.isNotBlank() == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S) // Design Tokens: 8dp
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Преподаватель",
                            modifier = Modifier.size(DesignIconSizes.Small), // Design Tokens: 20dp
                            tint = iconColor
                        )
                        Text(
                            text = test.teacher,
                            style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                            color = textSecondaryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignSpacing.S)) // Design Tokens: 8dp
                }
                
                // Аудитория и время
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S) // Design Tokens: 8dp
                ) {
                    if (test.classroom?.isNotBlank() == true) {
                        Icon(
                            Icons.Default.Room,
                            contentDescription = "Аудитория",
                            modifier = Modifier.size(DesignIconSizes.Small), // Design Tokens: 20dp
                            tint = iconColor
                        )
                        Text(
                            text = test.classroom,
                            style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                            color = textSecondaryColor
                        )
                    }
                    
                    if (test.time.isNotBlank()) {
                        Spacer(modifier = Modifier.width(DesignSpacing.Base)) // Design Tokens: 16dp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Время",
                                modifier = Modifier.size(DesignIconSizes.Small), // Design Tokens: 20dp
                                tint = iconColor
                            )
                            Text(
                                text = test.time,
                                style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                                color = textSecondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTestsCard() {
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
                // Иконка зачетов
                Icon(
                    Icons.Default.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp), // Design Tokens: крупная иконка для empty state
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Мягкий нейтральный цвет
                )
                
                Text(
                    text = "ЗАЧЕТЫ НЕ ЗАПЛАНИРОВАНЫ",
                    style = MaterialTheme.typography.headlineMedium, // Design Tokens: --type-h2: 20px/28px 600
                    fontWeight = FontWeight.SemiBold, // 600
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Расписание зачетов пока не опубликовано",
                    style = MaterialTheme.typography.bodyMedium, // Design Tokens: --type-small: 13px/18px 400
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

