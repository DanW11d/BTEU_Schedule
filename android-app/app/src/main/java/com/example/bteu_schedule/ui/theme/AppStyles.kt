package com.example.bteu_schedule.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Единые стили для всего приложения
 */
object AppStyles {
    
    // Скругления карточек
    val CardCornerRadius = 16.dp
    val CardCornerRadiusLarge = 20.dp
    val ButtonCornerRadius = 12.dp
    val HeaderCornerRadius = 24.dp
    
    // Отступы
    val CardPadding = 20.dp
    val CardPaddingLarge = 24.dp
    val ScreenPadding = 16.dp
    val ScreenPaddingLarge = 20.dp
    val ItemSpacing = 12.dp
    val ItemSpacingLarge = 16.dp
    val SectionSpacing = 24.dp
    
    // Elevation
    val CardElevation = 4.dp
    val CardElevationPressed = 8.dp
    
    // Размеры
    val IconSizeSmall = 18.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp
    val IconSizeXLarge = 40.dp
    val IconSizeXXLarge = 56.dp
    
    // Формы
    val CardShape = RoundedCornerShape(CardCornerRadius)
    val CardShapeLarge = RoundedCornerShape(CardCornerRadiusLarge)
    val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
    val HeaderShape = RoundedCornerShape(bottomStart = HeaderCornerRadius, bottomEnd = HeaderCornerRadius)
}

