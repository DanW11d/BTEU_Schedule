package com.example.bteu_schedule.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.MotionEasing
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.performMediumImpact
import com.example.bteu_schedule.ui.utils.isAnimationEnabled

/**
 * Вариант кнопки AppButton
 */
enum class AppButtonVariant {
    Primary,      // Основная кнопка с градиентом
    Secondary,    // Вторичная кнопка с обводкой
    Destructive   // Деструктивная кнопка (красная)
}

/**
 * Стиль кнопки
 */
private data class ButtonStyle(
    val backgroundGradient: List<Color>?,
    val backgroundColor: Color?,
    val textColor: Color,
    val borderColor: Color?,
    val shadowSpec: DesignShadows.ShadowSpec?
)

/**
 * AppButton — UI-KIT компонент
 * 
 * Варианты:
 * - Primary: основная кнопка с градиентом
 * - Secondary: вторичная кнопка с обводкой
 * - Destructive: деструктивная кнопка (красная)
 * 
 * Размеры:
 * - Высота: 48dp
 * - Радиус: 16dp
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hapticFeedback = rememberHapticFeedback() // A3.8: Виброотклик
    val colors = designColors()
    val colorScheme = MaterialTheme.colorScheme // Выносим из remember блока
    
    // A6.5: Анимации кнопок
    // A3.8: Виброотклик при нажатии
    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            // A6.5: Primary кнопка - haptic light
            if (variant == AppButtonVariant.Primary) {
                hapticFeedback.performLightImpact() // A6.5: haptic light для Primary
            } else if (variant == AppButtonVariant.Destructive) {
                hapticFeedback.performMediumImpact() // Destructive - medium
            } else {
                hapticFeedback.performLightImpact() // Secondary - light
            }
        }
    }
    
    // A6.5: Primary кнопка - scale 1 → 0.97 → 1, duration 120ms
    // A3.7: Плавная анимация нажатия
    // A3.10: Учитываем настройки доступности анимаций
    val animationEnabled = isAnimationEnabled()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f, // A6.5: scale 1 → 0.97 → 1
        animationSpec = if (animationEnabled) {
            if (variant == AppButtonVariant.Primary) {
                // A6.5: Primary кнопка - duration 120ms
                tween(
                    durationMillis = 120, // A6.5: duration 120ms
                    easing = MotionEasing.EaseOutCubic
                )
            } else {
                // Остальные кнопки - spring для плавности
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            }
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "buttonScale"
    )
    
    // A6.5: Secondary кнопка - изменение обводки при нажатии
    val borderWidth by animateDpAsState(
        targetValue = if (variant == AppButtonVariant.Secondary && isPressed && enabled) 2.dp else 1.dp, // A6.5: Изменение обводки (1dp → 2dp)
        animationSpec = tween(
            durationMillis = 120,
            easing = MotionEasing.EaseOutCubic
        ),
        label = "borderWidth"
    )
    
    // Определяем стили в зависимости от варианта
    val buttonStyle = remember(variant, enabled, colors, isPressed, colorScheme) {
        when (variant) {
            AppButtonVariant.Primary -> {
                if (enabled) {
                    // A3.7: Normal - градиент, Pressed - затемнение 10-20%
                    val gradientAlpha = if (isPressed) 0.85f else 1f // 15% затемнение при нажатии
                    // A6.5: Primary кнопка - тень усиливается при нажатии
                    val shadowSpec = if (isPressed) {
                        DesignShadows.High // A6.5: Тень усиливается (8dp → 12dp)
                    } else {
                        DesignShadows.Mid // Normal: 8dp
                    }
                    ButtonStyle(
                        backgroundGradient = listOf(
                            colors.primaryGradientStart.copy(alpha = gradientAlpha),
                            colors.primaryGradientEnd.copy(alpha = gradientAlpha)
                        ),
                        backgroundColor = null,
                        textColor = Color.White,
                        borderColor = null,
                        shadowSpec = shadowSpec // A6.5: Тень усиливается при нажатии
                    )
                } else {
                    // A3.7: Disabled - #A0A8B8 + альфа 30%
                    ButtonStyle(
                        backgroundGradient = null,
                        backgroundColor = Color(0xFFA0A8B8).copy(alpha = 0.3f),
                        textColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        borderColor = null,
                        shadowSpec = DesignShadows.Low
                    )
                }
            }
            AppButtonVariant.Secondary -> {
                // A6.5: Secondary кнопка - изменение обводки, лёгкое свечение градиента
                val glowAlpha = if (isPressed && enabled) 0.3f else 0f // A6.5: Лёгкое свечение градиента при нажатии
                ButtonStyle(
                    backgroundGradient = if (isPressed && enabled) {
                        // A6.5: Лёгкое свечение градиента при нажатии
                        listOf(
                            colors.primaryGradientStart.copy(alpha = glowAlpha),
                            colors.primaryGradientEnd.copy(alpha = glowAlpha)
                        )
                    } else {
                        null
                    },
                    backgroundColor = colorScheme.surface,
                    textColor = colorScheme.primary,
                    borderColor = colorScheme.primary,
                    shadowSpec = DesignShadows.Low
                )
            }
            AppButtonVariant.Destructive -> {
                ButtonStyle(
                    backgroundGradient = null,
                    backgroundColor = colorScheme.error,
                    textColor = Color.White,
                    borderColor = null,
                    shadowSpec = DesignShadows.Mid
                )
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignHeights.Button) // 48dp
            .scale(scale)
            .then(
                if (enabled) {
                    Modifier
                        .focusable() // A3.7: Поддержка Focused состояния
                        .clickable(
                            onClick = onClick,
                            interactionSource = interactionSource,
                            indication = null
                        )
                } else {
                    Modifier
                }
            )
            .then(
                if (buttonStyle.shadowSpec != null && enabled) {
                    Modifier.applyShadow(
                        shadowSpec = buttonStyle.shadowSpec!!,
                        shape = RoundedCornerShape(DesignRadius.M) // 16dp
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (buttonStyle.backgroundGradient != null) {
                    // Градиентный фон
                    Modifier.background(
                        brush = Brush.horizontalGradient(buttonStyle.backgroundGradient),
                        shape = RoundedCornerShape(DesignRadius.M) // 16dp
                    )
                } else if (buttonStyle.backgroundColor != null) {
                    // Однотонный фон
                    Modifier.background(
                        color = buttonStyle.backgroundColor,
                        shape = RoundedCornerShape(DesignRadius.M) // 16dp
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (buttonStyle.borderColor != null) {
                    // A6.5: Secondary кнопка - изменение обводки при нажатии
                    Modifier.border(
                        width = borderWidth, // A6.5: Анимированная ширина обводки (1dp → 2dp)
                        color = buttonStyle.borderColor,
                        shape = RoundedCornerShape(DesignRadius.M)
                    )
                } else {
                    Modifier
                }
            )
            .then(
                // A6.5: Secondary кнопка - лёгкое свечение градиента при нажатии
                if (variant == AppButtonVariant.Secondary && buttonStyle.backgroundGradient != null && enabled) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(buttonStyle.backgroundGradient),
                        shape = RoundedCornerShape(DesignRadius.M)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignSpacing.Base), // 16dp внутренний паддинг
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = buttonStyle.textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(DesignSpacing.S)) // 8dp отступ
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium, // Title: 16sp Medium
                fontWeight = FontWeight.Medium,
                color = buttonStyle.textColor
            )
        }
    }
}

