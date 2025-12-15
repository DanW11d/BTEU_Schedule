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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.isAnimationEnabled
import com.example.bteu_schedule.ui.utils.getAccessibleScaleAnimation

/**
 * Primary Card (Главная карточка раздела) — UI-KIT компонент
 * 
 * Свойства:
 * - icon: Icon
 * - title: String
 * - subtitle: String
 * - onTap: () → void
 * - trailing: Icon (обычно →)
 * 
 * Стиль:
 * - Радиус: 24dp
 * - Высота: 92dp
 * - Паддинги: 20dp
 * - Градиент фона
 */
@Composable
fun PrimaryCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onTap: () -> Unit,
    trailing: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    enabled: Boolean = true // A3.7: Disabled состояние
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

    // A3.7: Плавная анимация нажатия - scale 0.96 при нажатии, Disabled - альфа 50%
    // A3.10: Учитываем настройки доступности анимаций
    val animationEnabled = isAnimationEnabled()
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f // Disabled - без масштабирования
            isPressed -> 0.96f // A3.7: scale 0.96 при pressed
            else -> 1f
        },
        animationSpec = if (animationEnabled) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "scale"
    )

    // A3.7: Normal - тень 8dp, Pressed - тень 12dp
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 12f // A3.7: 12dp при pressed
            else -> 8f // A3.7: 8dp normal
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "elevation"
    )

    // A3.7: Disabled - альфа 50%
    val alpha by animateFloatAsState(
        targetValue = if (!enabled) 0.5f else 1f, // A3.7: альфа 50% для disabled
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp) // Min-height: 92dp (для адаптивности к тексту)
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
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(DesignRadius.L), // 24dp
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.horizontalGradient(gradientColors),
                shape = RoundedCornerShape(DesignRadius.L) // 24dp
            )
            .padding(DesignSpacing.CardPaddingLarge) // 20dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.IconTextSpacing) // 12dp - между иконкой и текстом
        ) {
            // Иконка слева: A3.4 - большие иконки в карточках 32dp, белая
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White, // Primary карточки: белая
                modifier = Modifier.size(DesignIconSizes.Large) // Design Tokens: большие иконки в карточках 32dp
            )

            // Текст
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 4dp
            ) {
                // Title: 18sp Bold
                // A3.6: Текст переносится при увеличении шрифта
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall, // Design Tokens: --type-h3: 18px/24px 600
                    fontWeight = FontWeight.SemiBold, // 600 (вместо Bold для соответствия Design Tokens)
                    color = Color.White,
                    maxLines = 2, // Максимум 2 строки при увеличении шрифта
                    overflow = TextOverflow.Ellipsis
                )
                
                // Subtitle: 14sp Regular
                // A3.2: Secondary текст на градиенте - белый с минимум 80% яркости (0.9f >= 0.8f ✅)
                // A3.6: Текст переносится при увеличении шрифта
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White.copy(alpha = 0.9f), // Минимум 80% для Secondary текста
                    maxLines = 2, // Максимум 2 строки при увеличении шрифта
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Trailing иконка (обычно →): A3.3 - стрелка 24dp в контейнере минимум 48dp
            Box(
                    modifier = Modifier.size(DesignIconSizes.IconButtonSize), // Design Tokens: 48dp (минимум для кликабельной зоны)
                contentAlignment = Alignment.Center
            ) {
                // A3.4 - стрелка 24dp, белая (Primary карточки)
                Icon(
                    imageVector = trailing,
                    contentDescription = "Перейти",
                    tint = Color.White, // Primary карточки: белая
                    modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: основной размер 24dp
                )
            }
        }
    }
}

