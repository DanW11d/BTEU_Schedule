package com.example.bteu_schedule.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Утилиты для адаптивных размеров экрана
 */
object ScreenUtils {
    
    /**
     * Получить адаптивный размер на основе размера экрана
     */
    @Composable
    fun getAdaptiveSize(baseSize: Dp): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val screenSize = minOf(screenWidth, screenHeight)
        
        // Масштабируем размер в зависимости от размера экрана
        // Базовый размер для экрана 360dp
        val scaleFactor = (screenSize.value / 360f).coerceIn(0.75f, 1.5f)
        return baseSize * scaleFactor
    }
    
    /**
     * Получить адаптивный размер иконки
     */
    @Composable
    fun getAdaptiveIconSize(baseSize: Dp = 24.dp): Dp {
        return getAdaptiveSize(baseSize)
    }
    
    /**
     * Получить адаптивный размер текста (в sp, но возвращаем Dp для использования в размерах)
     */
    @Composable
    fun getAdaptiveTextSize(baseSize: Dp = 16.dp): Dp {
        return getAdaptiveSize(baseSize)
    }
    
    /**
     * Получить адаптивный отступ
     */
    @Composable
    fun getAdaptivePadding(basePadding: Dp = 16.dp): Dp {
        return getAdaptiveSize(basePadding)
    }
    
    /**
     * Получить адаптивный размер карточки
     */
    @Composable
    fun getAdaptiveCardSize(baseSize: Dp = 48.dp): Dp {
        return getAdaptiveSize(baseSize)
    }
    
    /**
     * Определить, является ли экран маленьким
     */
    @Composable
    fun isSmallScreen(): Boolean {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return screenWidth < 360
    }
    
    /**
     * Определить, является ли экран большим (планшет)
     */
    @Composable
    fun isLargeScreen(): Boolean {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return screenWidth >= 600
    }
    
    /**
     * Получить количество колонок для сетки в зависимости от размера экрана
     */
    @Composable
    fun getColumnCount(): Int {
        return when {
            isLargeScreen() -> 3
            isSmallScreen() -> 1
            else -> 2
        }
    }
}

