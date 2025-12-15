package com.example.bteu_schedule.ui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.bteu_schedule.ui.theme.DarkColors
import com.example.bteu_schedule.ui.theme.LightColors
import com.example.bteu_schedule.ui.theme.DesignIconSizes

/**
 * Утилиты для унификации иконок
 * Материал Symbols Rounded стиль
 * Stroke: 1.75-2.0px
 * Размеры: Small (20px), Medium (24px), Large (32px)
 */

/**
 * Получить цвет для иконки в зависимости от темы и состояния
 */
@Composable
fun getIconColor(isActive: Boolean = false): Color {
    return if (isActive) {
        // Активные элементы - белые
        Color.White
    } else {
        // Вторичный цвет в зависимости от темы
        val isDark = isSystemInDarkTheme()
        if (isDark) {
            DarkColors.TextSecondary // #B0B3C1
        } else {
            LightColors.TextSecondary // #4B5563
        }
    }
}

/**
 * Получить стандартный размер иконки
 */
enum class IconSize {
    Small,   // 20px - в круглых кнопках
    Medium,  // 24px - обычные иконки
    Large    // 32px - в карточках факультетов
}

fun getIconSize(size: IconSize): Dp {
    return when (size) {
        IconSize.Small -> DesignIconSizes.Small
        IconSize.Medium -> DesignIconSizes.Medium
        IconSize.Large -> DesignIconSizes.Large
    }
}

