package com.example.bteu_schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Получить безопасный цвет текста для тёмной темы
 */
@Composable
fun safeTextColor(
    baseColor: androidx.compose.ui.graphics.Color,
    lightAlpha: Float = 1.0f,
    darkAlpha: Float = 1.0f
): androidx.compose.ui.graphics.Color {
    // FIX: Use isSystemInDarkTheme() for a reliable check
    val isDark = isSystemInDarkTheme()
    val alpha = if (isDark) {
        darkAlpha.coerceAtLeast(0.8f) // Минимум 80% для тёмной темы
    } else {
        lightAlpha
    }
    return baseColor.copy(alpha = alpha)
}

/**
 * Helper функции для доступа к цветам дизайн-системы в зависимости от темы
 */
@Composable
fun designColors(): DesignColorScheme {
    // Используем MaterialTheme.colorScheme для определения темы
    val isDark = MaterialTheme.colorScheme.background == DarkColors.BG
    return if (isDark) {
        DesignColorScheme(
            bg = DarkColors.BG,
            surface = DarkColors.Surface,
            surfaceAlt = DarkColors.SurfaceAlt,
            primary = DarkColors.Primary,
            primaryLight = DarkColors.PrimaryLight,
            primaryLight2 = DarkColors.PrimaryLight2,
            primaryPastel = DarkColors.PrimaryPastel,
            primaryGradientStart = DarkColors.PrimaryGradientStart,
            primaryGradientMid = DarkColors.PrimaryGradientMid,
            primaryGradientEnd = DarkColors.PrimaryGradientEnd,
            classicBlueStart = DarkColors.ClassicBlueStart,
            classicBlueEnd = DarkColors.ClassicBlueEnd,
            brightBlueStart = DarkColors.BrightBlueStart,
            brightBlueEnd = DarkColors.BrightBlueEnd,
            softBlueStart = DarkColors.SoftBlueStart,
            softBlueEnd = DarkColors.SoftBlueEnd,
            secondary = DarkColors.Secondary,
            textPrimary = DarkColors.TextPrimary,
            textSecondary = DarkColors.TextSecondary,
            textTertiary = DarkColors.TextTertiary,
            textOnPrimary = DarkColors.TextOnPrimary,
            border = DarkColors.Border,
            inputBorder = DarkColors.InputBorder,
            chipBG = DarkColors.ChipBG,
            chipActiveBG = DarkColors.ChipActiveBG,
            error = DarkColors.Error
        )
    } else {
        DesignColorScheme(
            bg = LightColors.BG,
            surface = LightColors.Surface,
            surfaceAlt = LightColors.SurfaceAlt,
            primary = LightColors.Primary,
            primaryLight = LightColors.PrimaryLight,
            primaryLight2 = LightColors.PrimaryLight2,
            primaryPastel = LightColors.PrimaryPastel,
            primaryGradientStart = LightColors.PrimaryGradientStart,
            primaryGradientMid = LightColors.PrimaryGradientMid,
            primaryGradientEnd = LightColors.PrimaryGradientEnd,
            classicBlueStart = LightColors.ClassicBlueStart,
            classicBlueEnd = LightColors.ClassicBlueEnd,
            brightBlueStart = LightColors.BrightBlueStart,
            brightBlueEnd = LightColors.BrightBlueEnd,
            softBlueStart = LightColors.SoftBlueStart,
            softBlueEnd = LightColors.SoftBlueEnd,
            secondary = LightColors.Secondary,
            textPrimary = LightColors.TextPrimary,
            textSecondary = LightColors.TextSecondary,
            textTertiary = LightColors.TextTertiary,
            textOnPrimary = LightColors.TextOnPrimary,
            border = LightColors.Border,
            inputBorder = LightColors.InputBorder,
            chipBG = LightColors.ChipBG,
            chipActiveBG = LightColors.ChipActiveBG,
            error = LightColors.Error
        )
    }
}

/**
 * Data class для хранения цветовой схемы
 */
data class DesignColorScheme(
    val bg: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val surfaceAlt: androidx.compose.ui.graphics.Color,
    val primary: androidx.compose.ui.graphics.Color,
    val primaryLight: androidx.compose.ui.graphics.Color,
    val primaryLight2: androidx.compose.ui.graphics.Color,
    val primaryPastel: androidx.compose.ui.graphics.Color,
    val primaryGradientStart: androidx.compose.ui.graphics.Color,
    val primaryGradientMid: androidx.compose.ui.graphics.Color,
    val primaryGradientEnd: androidx.compose.ui.graphics.Color,
    val classicBlueStart: androidx.compose.ui.graphics.Color,
    val classicBlueEnd: androidx.compose.ui.graphics.Color,
    val brightBlueStart: androidx.compose.ui.graphics.Color,
    val brightBlueEnd: androidx.compose.ui.graphics.Color,
    val softBlueStart: androidx.compose.ui.graphics.Color,
    val softBlueEnd: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val textOnPrimary: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val inputBorder: androidx.compose.ui.graphics.Color,
    val chipBG: androidx.compose.ui.graphics.Color,
    val chipActiveBG: androidx.compose.ui.graphics.Color,
    val error: androidx.compose.ui.graphics.Color
)
