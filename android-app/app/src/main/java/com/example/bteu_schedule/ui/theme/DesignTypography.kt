package com.example.bteu_schedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Design Tokens: Типографика
 * 
 * Рекомендации: стандартный scale, line-height 1.25–1.35, веса 400/600/700
 * 
 * Design Tokens:
 * - --type-h1: 24px/32px 700 (Bold)
 * - --type-h2: 20px/28px 600 (SemiBold)
 * - --type-h3: 18px/24px 600 (SemiBold)
 * - --type-body: 16px/22px 400 (Regular)
 * - --type-small: 13px/18px 400 (Regular)
 * 
 * Важно: не использовать line-height меньше, чем fontSize * 1.2
 * Для кастомных шрифтов проверять метрики (ascent/descent) и при необходимости увеличивать line-height
 */
val DesignTypography = Typography(
    // Design Tokens: --type-h1: 24px/32px 700
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 24.sp,
        lineHeight = 32.sp, // 1.33x (24 * 1.33 = 32)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-h2: 20px/28px 600
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 28.sp, // 1.4x (20 * 1.4 = 28)
        letterSpacing = 0.sp
    ),
    
    // Title: 16sp Medium - подзаголовки, пункты меню
    // lineHeight: 16 * 1.3 = 20.8sp ≈ 22sp (безопасное значение)
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp, // 1.375x (безопасное значение для предотвращения обрезания)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-body: 16px/22px 400 (основной текст)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 16.sp, // Design Tokens: 16px (обновлено с 14px)
        lineHeight = 22.sp, // 1.375x (16 * 1.375 = 22)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-small: 13px/18px 400
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 13.sp, // Design Tokens: 13px (обновлено с 12px)
        lineHeight = 18.sp, // 1.38x (13 * 1.38 = 18)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-h1: 24px/32px 700 (для экранов)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 24.sp,
        lineHeight = 32.sp, // 1.33x (24 * 1.33 = 32)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-h2: 20px/28px 600
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 28.sp, // 1.4x (20 * 1.4 = 28)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-h3: 18px/24px 600
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 18.sp, // Design Tokens: 18px
        lineHeight = 24.sp, // 1.33x (18 * 1.33 = 24)
        letterSpacing = 0.sp
    ),
    
    // Title: 16sp Medium
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp, // 1.375x (безопасное значение для предотвращения обрезания)
        letterSpacing = 0.sp
    ),
    
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp, // 1.375x (безопасное значение для предотвращения обрезания)
        letterSpacing = 0.sp
    ),
    
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp, // 1.375x (безопасное значение для предотвращения обрезания)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-body: 16px/22px 400
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 16.sp, // Design Tokens: 16px (обновлено с 14px)
        lineHeight = 22.sp, // 1.375x (16 * 1.375 = 22)
        letterSpacing = 0.sp
    ),
    
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp, // 1.375x (безопасное значение для предотвращения обрезания)
        letterSpacing = 0.sp
    ),
    
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 13.sp, // Design Tokens: 13px (обновлено с 12px)
        lineHeight = 18.sp, // 1.38x (13 * 1.38 = 18)
        letterSpacing = 0.sp
    ),
    
    // Design Tokens: --type-small: 13px/18px 400
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 13.sp, // Design Tokens: 13px (обновлено с 12px)
        lineHeight = 18.sp, // 1.38x (13 * 1.38 = 18)
        letterSpacing = 0.sp
    )
)
