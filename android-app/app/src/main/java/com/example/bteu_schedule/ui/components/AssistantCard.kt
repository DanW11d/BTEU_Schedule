package com.example.bteu_schedule.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.applyShadow

/**
 * Ассистент-карточка (ChatGPT-style) — UI-KIT компонент
 * 
 * Свойства:
 * - avatar: ImageVector или @Composable
 * - message: String
 * - timestamp: String?
 * - background (light/dark): автоматически через MaterialTheme
 * - optional: suggestions (кнопки)
 * 
 * Стиль:
 * - Радиус: 24dp
 * - Паддинг: 20dp
 * - Мягкая тень
 * - Анимация появления (fade + slide)
 */
@Composable
fun AssistantCard(
    avatar: @Composable () -> Unit,
    message: String,
    timestamp: String? = null,
    suggestions: List<String>? = null,
    onSuggestionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    visible: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Плавная анимация нажатия
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    // Используем ShadowLow для карточки ассистента
    // Light theme: 4dp blur, 5% black
    // Dark theme: 4dp blur, 40% blue tint
    val shadowSpec = DesignShadows.Low

    // Анимация появления (fade + slide)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { 20 },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        ) + slideOutVertically(
            targetOffsetY = { -20 },
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth() // 100% ширины
                .scale(scale)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            onClick = onClick,
                            interactionSource = interactionSource,
                            indication = null
                        )
                    } else {
                        Modifier
                    }
                )
                .applyShadow(
                    shadowSpec = shadowSpec,
                    shape = RoundedCornerShape(DesignRadius.L) // Радиус: 24dp
                )
                .background(
                    color = MaterialTheme.colorScheme.surface, // background (light/dark)
                    shape = RoundedCornerShape(DesignRadius.L) // Радиус: 24dp
                )
                .padding(DesignSpacing.CardPaddingLarge) // Паддинг: 20dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M), // 12dp между аватаром и контентом
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(40.dp)
                ) {
                    avatar()
                }

                // Основной контент
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 4dp между элементами
                ) {
                    // Message
                    // A3.6: Текст переносится при увеличении шрифта
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 10, // Достаточно строк для длинных сообщений
                        overflow = TextOverflow.Ellipsis
                    )

                    // Timestamp (если указан)
                    if (timestamp != null) {
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.labelSmall, // Caption: 12sp Regular
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Suggestions (опциональные кнопки)
                    if (suggestions != null && suggestions.isNotEmpty() && onSuggestionClick != null) {
                        Spacer(modifier = Modifier.height(DesignSpacing.S)) // 8dp отступ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S), // 8dp между кнопками
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            suggestions.forEach { suggestion ->
                                AppChip(
                                    text = suggestion,
                                    onClick = { onSuggestionClick(suggestion) },
                                    selected = false,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Перегрузка с ImageVector для аватара
 */
@Composable
fun AssistantCard(
    avatar: ImageVector,
    message: String,
    timestamp: String? = null,
    suggestions: List<String>? = null,
    onSuggestionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    visible: Boolean = true
) {
    AssistantCard(
        avatar = {
            Icon(
                imageVector = avatar,
                contentDescription = "Ассистент",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )
        },
        message = message,
        timestamp = timestamp,
        suggestions = suggestions,
        onSuggestionClick = onSuggestionClick,
        modifier = modifier,
        onClick = onClick,
        visible = visible
    )
}

/**
 * Перегрузка для обратной совместимости
 */
@Composable
fun AssistantCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val shadowSpec = DesignShadows.Low

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClick = onClick,
                        interactionSource = interactionSource,
                        indication = null
                    )
                } else {
                    Modifier
                }
            )
            .applyShadow(
                shadowSpec = shadowSpec,
                shape = RoundedCornerShape(DesignRadius.L)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(DesignRadius.L)
            )
            .padding(DesignSpacing.CardPaddingLarge)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.S)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (title == null && subtitle == null && icon == null) {
                content()
            } else {
                Spacer(modifier = Modifier.height(DesignSpacing.S))
                content()
            }
        }
    }
}

