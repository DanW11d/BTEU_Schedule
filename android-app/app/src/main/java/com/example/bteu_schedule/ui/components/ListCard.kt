package com.example.bteu_schedule.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.MotionEasing
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.isAnimationEnabled

/**
 * List Card (Карточка списка) — UI-KIT компонент
 * 
 * Используется в:
 * - Настройках
 * - Выборе факультетов
 * - Выборе группы
 * 
 * Свойства:
 * - icon: Icon
 * - title: String
 * - subtitle: String?
 * - trailing: Icon?
 * - onTap
 * 
 * Стиль:
 * - Радиус: 20dp
 * - Высота: 72dp
 * - Белый фон
 * - Мягкая тень
 */
@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: ImageVector? = null,
    onTap: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hapticFeedback = rememberHapticFeedback() // A3.8: Виброотклик
    
    // A3.8: Виброотклик при нажатии (Impact Light для всех кликабельных элементов)
    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            hapticFeedback.performLightImpact()
        }
    }

    // A6.3.2: Анимации выбора карты
    // Когда нажимается карточка:
    // - scale 1 → 0.96 → 1
    // - shadow увеличивается
    // - haptic feedback light (уже есть выше)
    // - duration 100–140ms (используем 120ms)
    val animationEnabled = isAnimationEnabled()
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f // Disabled - без масштабирования
            isPressed -> 0.96f // A6.3.2: scale 1 → 0.96 → 1
            else -> 1f
        },
        animationSpec = if (animationEnabled) {
            tween(
                durationMillis = 120, // A6.3.2: duration 100–140ms (используем 120ms)
                easing = MotionEasing.EaseOutCubic
            )
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "scale"
    )

    // A6.3.2: Shadow увеличивается при нажатии
    // A3.7: Normal - тень 8dp (DesignShadows.Mid), Pressed - тень 12dp (DesignShadows.High)
    val shadowSpec = if (isPressed) {
        DesignShadows.High // 12dp при pressed (shadow увеличивается)
    } else {
        DesignShadows.Mid // 8dp normal
    }

    // A3.7: Disabled - альфа 50%
    val alpha by animateFloatAsState(
        targetValue = if (!enabled) 0.5f else 1f, // A3.7: альфа 50% для disabled
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp) // Min-height: 72dp (для адаптивности к тексту)
            .scale(scale)
            .alpha(alpha) // A3.7: альфа 50% для disabled
            .then(
                if (enabled) {
                    Modifier
                        .focusable() // A3.7: Поддержка Focused состояния
                        .clickable(
                            onClick = onTap,
                            interactionSource = interactionSource,
                            indication = null
                        )
                } else {
                    Modifier
                }
            )
            .applyShadow(
                shadowSpec = shadowSpec,
                shape = RoundedCornerShape(DesignRadius.ListCard) // Радиус: 12dp (Design Tokens: --radius-md)
            )
            .background(
                color = surfaceColor, // Белый фон (Light) / тёмный синий (Dark)
                shape = RoundedCornerShape(DesignRadius.ListCard) // Радиус: 12dp (Design Tokens: --radius-md)
            )
            .padding(DesignSpacing.CardPadding) // 16dp внутренний паддинг
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка слева: A3.4 - основной размер 24dp, цвет для списков/настроек
            // Light: #4C6CFF, Dark: #7A8BFF
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG) {
                    Color(0xFF7A8BFF) // Dark: #7A8BFF
                } else {
                    Color(0xFF4C6CFF) // Light: #4C6CFF
                },
                modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: основной размер 24dp
            )
            
            // Отступ между иконкой и текстом: 12dp
            Spacer(modifier = Modifier.width(DesignSpacing.IconTextSpacing))

            // Основной контент
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 4dp
            ) {
                // A3.6: Текст переносится при увеличении шрифта
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // Title: 16sp Medium
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, // Максимум 2 строки при увеличении шрифта
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    // A3.6: Текст переносится при увеличении шрифта
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, // Одна строка для подзаголовка
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing иконка (если указана): A3.3 - стрелка 24dp в контейнере минимум 48dp
            if (trailing != null) {
                Spacer(modifier = Modifier.width(DesignSpacing.Base)) // 16dp отступ
                Box(
                    modifier = Modifier.size(DesignIconSizes.IconButtonSize), // Design Tokens: 48dp (минимум для кликабельной зоны)
                    contentAlignment = Alignment.Center
                ) {
                    // A3.4 - стрелка использует тот же цвет, что и основная иконка
                    Icon(
                        imageVector = trailing,
                        contentDescription = "Перейти",
                        tint = if (MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG) {
                            Color(0xFF7A8BFF) // Dark: #7A8BFF
                        } else {
                            Color(0xFF4C6CFF) // Light: #4C6CFF
                        },
                        modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: основной размер 24dp
                    )
                }
            }
        }
    }
}

/**
 * Перегрузка для обратной совместимости
 * Использует content lambda для кастомного контента
 */
@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
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

    val surfaceColor = MaterialTheme.colorScheme.surface
    // Используем ShadowLow для списочных карточек (4dp blur, 5% black)
    val shadowSpec = DesignShadows.Low

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp) // Min-height: 72dp (для адаптивности к тексту)
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
                shape = RoundedCornerShape(DesignRadius.ListCard) // Радиус: 12dp (Design Tokens: --radius-md)
            )
            .background(
                color = surfaceColor,
                shape = RoundedCornerShape(DesignRadius.ListCard) // Радиус: 12dp (Design Tokens: --radius-md)
            )
            .padding(DesignSpacing.CardPadding) // 16dp внутренний паддинг
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
            // Отступы контролируются явно в content для точного соответствия спецификации
        ) {
            content()
        }
    }
}

/**
 * Альтернативная версия ListCard с явным указанием контента
 */
@Composable
fun ListCardContent(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    title: String? = null,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListCard(
        modifier = modifier,
        onClick = onClick
    ) {
        // Leading content (иконка или другой элемент)
        if (leadingContent != null) {
            leadingContent()
            // Отступ между иконкой и текстом: 12dp
            Spacer(modifier = Modifier.width(DesignSpacing.IconTextSpacing))
        }

        // Основной контент
        if (title != null || subtitle != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 4dp
            ) {
                if (title != null) {
                    androidx.compose.material3.Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (subtitle != null) {
                    androidx.compose.material3.Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Trailing content (стрелка или другой элемент)
        if (trailingContent != null) {
            // Отступ между текстом и trailing content
            if (title != null || subtitle != null) {
                Spacer(modifier = Modifier.width(DesignSpacing.Base)) // 16dp
            }
            trailingContent()
        }
    }
}

