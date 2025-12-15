package com.example.bteu_schedule.ui.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

/**
 * Утилиты для доступности и проверки соответствия стандартам
 * WCAG 2.1 AA, Material Design Accessibility, Apple HIG Accessibility
 */

/**
 * Вычислить относительную яркость цвета (по WCAG)
 * Значение от 0 (черный) до 1 (белый)
 */
fun Color.relativeLuminance(): Double {
    val r = if (red <= 0.03928) red / 12.92 else ((red + 0.055) / 1.055).pow(2.4)
    val g = if (green <= 0.03928) green / 12.92 else ((green + 0.055) / 1.055).pow(2.4)
    val b = if (blue <= 0.03928) blue / 12.92 else ((blue + 0.055) / 1.055).pow(2.4)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * Вычислить контраст между двумя цветами (WCAG)
 * Возвращает значение от 1:1 (нет контраста) до 21:1 (максимальный контраст)
 */
fun contrastRatio(color1: Color, color2: Color): Double {
    val l1 = color1.relativeLuminance()
    val l2 = color2.relativeLuminance()
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * Проверить, соответствует ли контраст стандарту WCAG 2.1 AA
 * 
 * @param textColor Цвет текста
 * @param backgroundColor Цвет фона
 * @param isLargeText true для крупного текста (18sp+ или 14sp Bold), false для обычного
 * @return true если контраст соответствует WCAG AA (4.5:1 для обычного, 3:1 для крупного)
 */
@Composable
fun meetsWCAGAA(
    textColor: Color,
    backgroundColor: Color,
    isLargeText: Boolean = false
): Boolean {
    val ratio = contrastRatio(textColor, backgroundColor)
    return if (isLargeText) {
        ratio >= 3.0 // WCAG AA для крупного текста
    } else {
        ratio >= 4.5 // WCAG AA для обычного текста
    }
}

/**
 * Получить безопасный цвет текста с достаточным контрастом
 * 
 * @param textColor Предпочтительный цвет текста
 * @param backgroundColor Цвет фона
 * @param isLargeText true для крупного текста
 * @return Цвет текста с достаточным контрастом или исходный цвет
 */
@Composable
fun getAccessibleTextColor(
    textColor: Color,
    backgroundColor: Color,
    isLargeText: Boolean = false
): Color {
    return if (meetsWCAGAA(textColor, backgroundColor, isLargeText)) {
        textColor
    } else {
        // Если контраст недостаточен, возвращаем более контрастный вариант
        val isDark = backgroundColor.relativeLuminance() < 0.5
        if (isDark) {
            Color.White // Белый на темном фоне
        } else {
            Color.Black // Черный на светлом фоне
        }
    }
}

/**
 * Минимальные размеры для доступности
 */
object AccessibilitySizes {
    /**
     * Минимальная область касания (Material Design)
     */
    val MinTouchTarget = 48.dp
    
    /**
     * Минимальная область касания (Apple HIG)
     */
    val MinTouchTargetApple = 44.dp
    
    /**
     * Минимальный размер текста
     */
    val MinTextSize = 12.sp
    
    /**
     * Рекомендуемый размер основного текста
     */
    val RecommendedTextSize = 14.sp
    
    /**
     * Минимальный отступ между интерактивными элементами
     */
    val MinSpacingBetweenInteractive = 8.dp
}

/**
 * Проверить, соответствует ли размер элемента требованиям доступности
 * 
 * @param size Размер элемента
 * @param isInteractive true если элемент интерактивный
 * @return true если размер соответствует требованиям
 */
fun meetsAccessibilitySize(size: androidx.compose.ui.unit.Dp, isInteractive: Boolean): Boolean {
    return if (isInteractive) {
        size >= AccessibilitySizes.MinTouchTarget
    } else {
        true // Для неинтерактивных элементов нет минимального размера
    }
}

/**
 * Получить безопасный размер для интерактивного элемента
 * 
 * @param preferredSize Предпочтительный размер
 * @return Размер не менее минимального требования
 */
fun getAccessibleSize(preferredSize: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp {
    return maxOf(preferredSize, AccessibilitySizes.MinTouchTarget)
}

