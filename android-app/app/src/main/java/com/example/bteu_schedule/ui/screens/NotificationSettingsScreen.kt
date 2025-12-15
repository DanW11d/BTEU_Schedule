package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.designColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NotificationSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var remindersEnabled by remember { mutableStateOf(true) }
    var scheduleUpdatesEnabled by remember { mutableStateOf(true) }
    var examsEnabled by remember { mutableStateOf(true) }
    var announcementsEnabled by remember { mutableStateOf(true) }
    
    var selectedReminderTime by remember { mutableStateOf(15) } // в минутах
    val reminderTimes = listOf(5, 10, 15, 30, 60)
    
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        isScreenVisible = true
    }

    val colors = designColors()

    // Полноэкранный режим: фон идет на весь экран, включая под статус-бар и системную панель
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
            // Адаптивный хедер с фиксированной высотой и единым градиентом (как в остальном приложении)
            AdaptiveHeader(
                title = "Настройки уведомлений",
                subtitle = "Управление уведомлениями",
                isVisible = isScreenVisible,
                isScrolled = false,
                backButton = true,
                onBackClick = onBack,
                headerType = HeaderType.NAVIGATION
            )

            // Контент - начинается сразу после хедера
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Белый фон для контента
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DesignSpacing.Base), // Design Tokens: 16dp
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp
        ) {
            Spacer(modifier = Modifier.height(DesignSpacing.XL)) // Design Tokens: 24dp (отступ после хедера)
            
            // Общее включение уведомлений
            AnimatedVisibility(
                visible = isScreenVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 150)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring()
                        )
            ) {
                NotificationSettingsSectionCard(
                    title = "Уведомления",
                    icon = Icons.Default.Notifications
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (notificationsEnabled) "Включены" else "Выключены",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (notificationsEnabled) "Вы будете получать уведомления" else "Уведомления отключены",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }

            // Типы уведомлений
            AnimatedVisibility(
                visible = isScreenVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring()
                        )
            ) {
                NotificationSettingsSectionCard(
                    title = "Типы уведомлений",
                    icon = Icons.Default.Settings
                ) {
                        // Напоминания о парах
                    NotificationTypeItem(
                    icon = Icons.Default.Schedule,
                    title = "Напоминания о парах",
                    subtitle = "Уведомления перед занятиями",
                    isEnabled = remindersEnabled && notificationsEnabled,
                    onToggle = { remindersEnabled = it },
                    enabled = notificationsEnabled
                )

                    HorizontalDivider(
                    modifier = Modifier.padding(horizontal = DesignSpacing.Base), // Design Tokens: 16dp
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                    // Выбор времени напоминания
                    if (remindersEnabled && notificationsEnabled) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
                    ) {
                        Text(
                            text = "Напомнить за:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S), // Design Tokens: 8dp
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            reminderTimes.forEach { minutes ->
                                FilterChip(
                                    selected = selectedReminderTime == minutes,
                                    onClick = { selectedReminderTime = minutes },
                                    label = { 
                                        Text(
                                            text = when (minutes) {
                                                60 -> "1 час"
                                                else -> "$minutes мин"
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                    
                        HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = DesignSpacing.Base, // Design Tokens: 16dp
                            vertical = DesignSpacing.S // Design Tokens: 8dp
                        ),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }

                    // Изменения расписания
                    NotificationTypeItem(
                    icon = Icons.Default.Update,
                    title = "Изменения расписания",
                    subtitle = "Обновления и замены",
                    isEnabled = scheduleUpdatesEnabled && notificationsEnabled,
                    onToggle = { scheduleUpdatesEnabled = it },
                    enabled = notificationsEnabled
                )

                    HorizontalDivider(
                    modifier = Modifier.padding(horizontal = DesignSpacing.Base), // Design Tokens: 16dp
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                    // Экзамены и зачёты
                    NotificationTypeItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "Экзамены и зачёты",
                    subtitle = "Уведомления о сессии",
                    isEnabled = examsEnabled && notificationsEnabled,
                    onToggle = { examsEnabled = it },
                    enabled = notificationsEnabled
                )

                    HorizontalDivider(
                    modifier = Modifier.padding(horizontal = DesignSpacing.Base), // Design Tokens: 16dp
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                    // Важные объявления
                    NotificationTypeItem(
                    icon = Icons.Default.Campaign,
                    title = "Важные объявления",
                    subtitle = "От университета",
                    isEnabled = announcementsEnabled && notificationsEnabled,
                    onToggle = { announcementsEnabled = it },
                    enabled = notificationsEnabled
                )
            }
            }

            // Предварительный просмотр
            AnimatedVisibility(
                visible = isScreenVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 250)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring()
                        )
            ) {
                NotificationSettingsSectionCard(
                    title = "Предварительный просмотр",
                    icon = Icons.Default.Visibility
                ) {
                    val previewParts = mutableListOf<String>()
                if (remindersEnabled && notificationsEnabled) {
                    previewParts.add("Напоминание о паре за $selectedReminderTime мин")
                }
                if (scheduleUpdatesEnabled && notificationsEnabled) {
                    previewParts.add("Изменения расписания")
                }
                if (examsEnabled && notificationsEnabled) {
                    previewParts.add("Экзамены")
                }
                if (announcementsEnabled && notificationsEnabled) {
                    previewParts.add("Важные объявления")
                }

                if (previewParts.isEmpty()) {
                    Text(
                        text = "Уведомления отключены",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = DesignSpacing.S) // Design Tokens: 8dp
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.S) // Design Tokens: 8dp
                    ) {
                        previewParts.forEach { part ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(DesignIconSizes.Small) // 20dp (ближайшее к 18dp)
                                )
                                Text(
                                    text = part,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            }

            // Кнопка сохранения
            AnimatedVisibility(
                visible = isScreenVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring()
                        )
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignSpacing.S), // Design Tokens: 8dp
                    shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Сохранить настройки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = DesignSpacing.XS) // Design Tokens: 4dp
                    )
                }
            }
            }
        }
    }
}


@Composable
private fun NotificationSettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = DesignSpacing.XS, // Design Tokens: 4dp (легкая тень как в ModernGradientCard)
                shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp
                spotColor = Color(0x0F0D1333) // rgba(13,19,51,0.06) - единый цвет тени
            ),
                    shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp (ближайшее к 20dp)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignSpacing.L), // Design Tokens: 20dp
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок секции с иконкой
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(DesignIconSizes.Medium) // 24dp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            
            // Содержимое секции
            content()
        }
    }
}

@Composable
fun NotificationTypeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignRadius.S)), // Design Tokens: 12dp
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = DesignSpacing.M, // Design Tokens: 12dp
                    horizontal = DesignSpacing.XS // Design Tokens: 4dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base), // Design Tokens: 16dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка с фоном
                Box(
                    modifier = Modifier
                        .size(DesignIconSizes.Large) // 32dp (ближайшее к 40dp, но используем стандартный размер)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Используем primary цвет из темы
                            shape = RoundedCornerShape(DesignRadius.XS) // Design Tokens: 8dp (ближайшее к 10dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(DesignIconSizes.Small) // 20dp (ближайшее к 22dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                enabled = enabled
            )
        }
    }
}

