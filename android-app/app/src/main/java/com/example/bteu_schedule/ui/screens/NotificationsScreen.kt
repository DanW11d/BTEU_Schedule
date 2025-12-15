package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.NotificationUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import com.example.bteu_schedule.ui.viewmodel.NotificationViewModel
import com.example.bteu_schedule.ui.viewmodel.NotificationUiState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val colors = designColors()
    val uiState by viewModel.uiState.collectAsState()
    
    // Анимация появления экрана
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isScreenVisible = true
    }
    
    val listState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    // ✅ Используем MaterialTheme.colorScheme.background вместо градиента для единообразия
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
            AdaptiveHeader(
                title = "Уведомления",
                subtitle = null,
                isVisible = isScreenVisible,
                isScrolled = isScrolled.value,
                currentGroup = null,
                onProfileClick = onProfileClick
            )
            
            // Контент уведомлений - начинается сразу после градиента хедера
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NotificationContent(uiState = uiState, viewModel = viewModel, listState = listState)
            }
        }
    }
}

@Composable
private fun NotificationContent(
    uiState: NotificationUiState,
    viewModel: NotificationViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    AnimatedVisibility(
        visible = uiState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (!uiState.isLoading) {
        if (uiState.notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // ✅ Центрирование по вертикали и горизонтали
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignSpacing.Base)
                        .shadow(
                            elevation = 2.dp, // ✅ Добавлена лёгкая тень для визуального разделения
                            shape = RoundedCornerShape(DesignRadius.M),
                            spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(DesignRadius.M),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp), // Design Tokens: крупная иконка для empty state
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Мягкий нейтральный цвет
                        )
                        Text(
                            "УВЕДОМЛЕНИЙ НЕТ",
                            style = MaterialTheme.typography.headlineMedium, // Design Tokens: --type-h2: 20px/28px 600
                            fontWeight = FontWeight.SemiBold, // 600
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "У вас нет новых уведомлений",
                            style = MaterialTheme.typography.bodyMedium, // Design Tokens: --type-small: 13px/18px 400
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavigationHeight = 100.dp
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = DesignSpacing.M,
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base,
                    bottom = DesignSpacing.XL + bottomNavigationHeight + navigationBarsPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
            ) {
                itemsIndexed(uiState.notifications) { index, notification ->
                    AnimatedVisibility(
                        visible = true,
                        enter = premiumCardAppearAnimation(index),
                        exit = fadeOut()
                    ) {
                        NotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationUi,
    onMarkAsRead: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "cardPress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = onMarkAsRead,
                interactionSource = interactionSource,
                indication = null
            ),
        shape = RoundedCornerShape(DesignRadius.M), // 16px для карточек
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
        ) {
            // Иконка с фоном
            Box(
                modifier = Modifier
                    .size(DesignIconSizes.IconButtonSize)
                    .background(
                        color = if (notification.isRead) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(DesignRadius.M)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(DesignIconSizes.Medium)
                )
            }
            
            // Текст
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2, // Dynamic Type: перенос текста при увеличении шрифта
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(DesignSpacing.S)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }
                }
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3, // Dynamic Type: перенос текста при увеличении шрифта
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
