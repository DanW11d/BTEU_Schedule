package com.example.bteu_schedule.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.applyShadow

/**
 * AppChip — UI-KIT компонент
 * 
 * Использование:
 * - Фильтры расписания
 * - Подгруппы
 * - Типы занятий
 * 
 * Размер:
 * - Высота: 32dp
 * - Радиус: 12dp
 */
@Composable
fun AppChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Плавная анимация нажатия
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chipScale"
    )
    
    // Анимация цвета фона
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "chipBackground"
    )
    
    // Анимация цвета текста
    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            selected -> Color.White
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "chipText"
    )
    
    // Тень только для выбранного чипа
    val shadowSpec = if (selected && enabled) {
        DesignShadows.Low
    } else {
        null
    }
    
    Box(
        modifier = modifier
            .height(32.dp) // Высота: 32dp
            .scale(scale)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        onClick = onClick,
                        interactionSource = interactionSource,
                        indication = null
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (shadowSpec != null) {
                    Modifier.applyShadow(
                        shadowSpec = shadowSpec,
                        shape = RoundedCornerShape(DesignRadius.S) // 12dp
                    )
                } else {
                    Modifier
                }
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(DesignRadius.S) // 12dp
            )
            .padding(horizontal = DesignSpacing.M, vertical = DesignSpacing.XS) // 12dp горизонтально, 4dp вертикально
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = textColor
        )
    }
}

