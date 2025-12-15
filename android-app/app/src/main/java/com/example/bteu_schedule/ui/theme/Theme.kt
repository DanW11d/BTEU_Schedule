package com.example.bteu_schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    secondary = DarkColors.Secondary,
    tertiary = DarkColors.Secondary,
    background = DarkGray,      // #262626
    surface = AlmostBlack, // #141414
    surfaceVariant = DarkColors.SurfaceAlt,
    primaryContainer = DarkColors.Primary.copy(alpha = 0.2f),
    onPrimary = DarkColors.TextOnPrimary,
    onSecondary = DarkColors.TextPrimary,
    onTertiary = DarkColors.TextPrimary,
    onBackground = DarkColors.TextPrimary,
    onSurface = DarkColors.TextPrimary,
    onSurfaceVariant = DarkColors.TextSecondary,
    error = DarkColors.Error,
    onError = DarkColors.TextOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    secondary = LightColors.Secondary,
    tertiary = LightColors.Secondary,
    background = LightColors.BG,
    surface = LightColors.Surface,
    surfaceVariant = LightColors.SurfaceAlt,
    primaryContainer = LightColors.Primary.copy(alpha = 0.1f),
    onPrimary = LightColors.TextOnPrimary,
    onSecondary = LightColors.TextPrimary,
    onTertiary = LightColors.TextPrimary,
    onBackground = LightColors.TextPrimary,
    onSurface = LightColors.TextPrimary,
    onSurfaceVariant = LightColors.TextSecondary,
    error = LightColors.Error,
    onError = LightColors.TextOnPrimary
)

@Composable
fun BTEU_ScheduleTheme(
    themeManager: ThemeManager,
    dynamicColor: Boolean = false, // Отключаем dynamicColor для использования нашей дизайн-системы
    content: @Composable () -> Unit
) {
    val themeMode by themeManager.themeMode.collectAsState()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DesignTypography,
        content = content
    )
}
