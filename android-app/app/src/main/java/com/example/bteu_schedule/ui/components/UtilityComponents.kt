package com.example.bteu_schedule.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performErrorFeedback
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performErrorFeedback

/**
 * Skeleton (Плейсхолдер) — UI-KIT компонент
 * 
 * A6.7: Анимации загрузки (Loading)
 * 
 * Стиль:
 * - Радиус: 16dp
 * - Анимация shimmer
 * - Speed: 1800ms
 * - Direction: left → right
 * - Цвет:
 *   - светлая тема: #E6ECF9
 *   - тёмная тема: #1C2033
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp
) {
    // A6.7: Определяем тему для выбора цвета
    val isDarkTheme = isSystemInDarkTheme()
    
    // A6.7: Анимация shimmer - Speed: 1800ms, Direction: left → right
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800, // A6.7: Speed: 1800ms
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    // A6.7: Цвета для shimmer
    // светлая тема: #E6ECF9
    // тёмная тема: #1C2033
    val baseColor = if (isDarkTheme) {
        Color(0xFF1C2033) // A6.7: тёмная тема: #1C2033
    } else {
        Color(0xFFE6ECF9) // A6.7: светлая тема: #E6ECF9
    }
    
    // Highlight цвет для shimmer эффекта (немного светлее базового)
    val highlightColor = if (isDarkTheme) {
        baseColor.copy(alpha = 0.6f) // Для тёмной темы используем альфа
    } else {
        baseColor.copy(alpha = 0.8f) // Для светлой темы используем альфа
    }

    Box(
        modifier = modifier
            .then(
                if (width != null) {
                    Modifier.width(width)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(height)
            .clip(RoundedCornerShape(DesignRadius.M)) // Радиус: 16dp
            .background(baseColor)
    ) {
        // A6.7: Shimmer эффект - движущийся градиент слева направо
        // Используем ширину контейнера для правильного расчета offset
        val shimmerWidth = 400f // Ширина shimmer эффекта
        val shimmerOffset = shimmerProgress * (1200f + shimmerWidth) - shimmerWidth / 2
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            highlightColor,
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset, 0f), // A6.7: Direction: left → right
                        end = Offset(shimmerOffset + shimmerWidth, 0f)
                    )
                )
        )
    }
}

/**
 * ErrorState (Ошибки) — UI-KIT компонент
 * 
 * Элементы:
 * - Иконка ⚠
 * - Текст
 * - Кнопка «Повторить»
 * 
 * A6.9: Ошибки - 2× Light (двойной лёгкий виброотклик)
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Warning
) {
    val hapticFeedback = rememberHapticFeedback() // A6.9: Виброотклик для ошибок
    
    // A6.9: Ошибки - 2× Light при появлении
    LaunchedEffect(Unit) {
        hapticFeedback.performErrorFeedback()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp отступ
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // 16dp между элементами
    ) {
        // Иконка ⚠
        Icon(
            imageVector = icon,
            contentDescription = "Ошибка",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        // Текст
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Кнопка «Повторить»
        AppButton(
            text = "Повторить",
            onClick = onRetry,
            variant = AppButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * EmptyState (Пустые состояния) — UI-KIT компонент
 * 
 * Элементы:
 * - Иллюстрация
 * - Заголовок
 * - Подзаголовок
 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String? = null,
    illustration: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp отступ
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // 16dp между элементами
    ) {
        // Иллюстрация
        if (illustration != null) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                illustration()
            }
        }

        // Заголовок
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium, // H2: 20sp SemiBold
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Подзаголовок
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

