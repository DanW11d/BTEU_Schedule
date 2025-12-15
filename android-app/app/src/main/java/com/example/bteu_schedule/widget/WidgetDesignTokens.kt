package com.example.bteu_schedule.widget

import android.content.Context
import android.content.res.Configuration

/**
 * A7.2: Дизайн-токены для виджетов
 * 
 * Универсальные правила дизайна для всех виджетов
 */

object WidgetDesignTokens {
    
    /**
     * A7.2: Основной фон
     * Light: #FFFFFF, radius 24dp, shadow + light stroke #E6ECF9
     * Dark: #0C0F25, radius 24dp, stroke #1A1F3A
     * 
     * A7.3: Тени
     * Light: rgba(0,0,0,0.08), blur 18dp
     * Dark: rgba(0,0,0,0.50), blur 22dp
     */
    object Background {
        const val RADIUS_DP = 24f
        
        // Light Theme
        const val COLOR_LIGHT = 0xFFFFFFFF.toInt() // #FFFFFF
        const val STROKE_LIGHT = 0xFFE6ECF9.toInt() // #E6ECF9
        const val SHADOW_OPACITY_LIGHT = 0x14 // A7.3: rgba(0,0,0,0.08) = 8% opacity
        const val SHADOW_BLUR_LIGHT_DP = 18f // A7.3: blur 18dp
        
        // Dark Theme
        const val COLOR_DARK = 0xFF0C0F25.toInt() // #0C0F25
        const val STROKE_DARK = 0xFF1A1F3A.toInt() // #1A1F3A
        const val SHADOW_OPACITY_DARK = 0x80 // A7.3: rgba(0,0,0,0.50) = 50% opacity
        const val SHADOW_BLUR_DARK_DP = 22f // A7.3: blur 22dp
    }
    
    /**
     * A7.3: Акцент (главный)
     * Градиент: #4C6CFF → #000064
     */
    object Accent {
        const val GRADIENT_START = 0xFF4C6CFF.toInt() // #4C6CFF
        const val GRADIENT_END = 0xFF000064.toInt() // #000064
        const val GRADIENT_ANGLE = 0 // Горизонтальный (слева направо)
    }
    
    /**
     * A7.2: Внутренний паддинг
     * 16–20dp
     */
    object Padding {
        const val STANDARD_DP = 16f
        const val LARGE_DP = 20f
        const val COMPACT_DP = 12f
        const val BETWEEN_ELEMENTS_DP = 8f
        const val BETWEEN_ELEMENTS_LARGE_DP = 12f
    }
    
    /**
     * A7.2: Текст
     * Заголовок: 16sp Medium
     * Основной текст: 14sp Regular
     * Реплика ассистента: 15sp Medium
     * Подписи: 12sp Regular
     */
    object Typography {
        const val HEADLINE_SIZE_SP = 16f
        const val BODY_SIZE_SP = 14f
        const val ASSISTANT_MESSAGE_SIZE_SP = 15f
        const val CAPTION_SIZE_SP = 12f
    }
    
    /**
     * A7.2: Иконки
     * Размер: 20–24dp
     * Акцент: #4C6CFF (light) или #3A4DFF (dark) - A7.9: глубокий, насыщенный, контрастный
     */
    object Icons {
        const val SIZE_STANDARD_DP = 24f
        const val SIZE_COMPACT_DP = 20f
        const val SIZE_LARGE_DP = 32f
        
        // Light Theme
        const val ACCENT_LIGHT = 0xFF4C6CFF.toInt() // #4C6CFF
        
        // Dark Theme - A7.9: акцент #3A4DFF (глубокий, насыщенный, контрастный)
        const val ACCENT_DARK = 0xFF3A4DFF.toInt() // #3A4DFF (A7.9: изменено с #7A8BFF)
    }
    
    /**
     * A7.9: Тёмная тема виджета
     * Dark Mode должен выглядеть: глубоким, насыщенным, контрастным
     * 
     * Пример:
     * - фон: #0C0F25
     * - текст: #FFFFFF
     * - акцент: #3A4DFF
     * - тень: чёрная 50%
     */
    object DarkTheme {
        const val BACKGROUND = 0xFF0C0F25.toInt() // #0C0F25 - глубокий, насыщенный фон
        const val TEXT_PRIMARY = 0xFFFFFFFF.toInt() // #FFFFFF - контрастный белый текст
        const val TEXT_SECONDARY = 0xFF9CA3AF.toInt() // #9CA3AF - мягкий серый для вторичного текста
        const val ACCENT = 0xFF3A4DFF.toInt() // #3A4DFF - глубокий, насыщенный акцент
        const val SHADOW_COLOR = 0x80000000.toInt() // rgba(0,0,0,0.50) - чёрная тень 50%
        const val STROKE = 0xFF1A1F3A.toInt() // #1A1F3A - мягкая обводка
    }
    
    /**
     * A7.10: Максимальная читаемость
     * - Без лишних элементов
     * - Большие отступы
     * - Мягкие цвета
     * - Разборчивый текст
     * - Акцент на одном сообщении
     */
    object Readability {
        // A7.10: Большие отступы для максимальной читаемости
        const val PADDING_LARGE_DP = 20f // Большие отступы
        const val PADDING_EXTRA_LARGE_DP = 24f // Очень большие отступы
        const val SPACING_BETWEEN_ELEMENTS_DP = 16f // Пространство между элементами
        
        // A7.10: Мягкие цвета для комфортного чтения
        const val TEXT_SOFT_LIGHT = 0xFF6B7280.toInt() // Мягкий серый для светлой темы
        const val TEXT_SOFT_DARK = 0xFF9CA3AF.toInt() // Мягкий серый для тёмной темы
        
        // A7.10: Разборчивый текст - минимальные размеры
        const val TEXT_MIN_SIZE_SP = 14f // Минимальный размер для разборчивости
        const val TEXT_READABLE_SIZE_SP = 16f // Оптимальный размер для чтения
        
        // A7.10: Акцент на одном сообщении - максимальная контрастность
        const val MESSAGE_HIGHLIGHT_ALPHA = 1.0f // Полная непрозрачность для акцента
        const val BACKGROUND_SUBTLE_ALPHA = 0.95f // Лёгкая прозрачность для фона
    }
    
    /**
     * Определяет, используется ли тёмная тема
     */
    fun isDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * Получить цвет фона в зависимости от темы
     */
    fun getBackgroundColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Background.COLOR_DARK
        } else {
            Background.COLOR_LIGHT
        }
    }
    
    /**
     * Получить цвет обводки в зависимости от темы
     */
    fun getStrokeColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Background.STROKE_DARK
        } else {
            Background.STROKE_LIGHT
        }
    }
    
    /**
     * Получить акцентный цвет иконок в зависимости от темы
     * A7.9: Для тёмной темы используется #3A4DFF (глубокий, насыщенный, контрастный)
     */
    fun getIconAccentColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Icons.ACCENT_DARK // A7.9: #3A4DFF
        } else {
            Icons.ACCENT_LIGHT
        }
    }
    
    /**
     * A7.9: Получить цвет текста для тёмной темы
     * Контрастный белый текст (#FFFFFF) для максимальной читаемости
     */
    fun getTextColor(context: Context, isPrimary: Boolean = true): Int {
        return if (isDarkTheme(context)) {
            if (isPrimary) {
                DarkTheme.TEXT_PRIMARY // A7.9: #FFFFFF - контрастный белый
            } else {
                DarkTheme.TEXT_SECONDARY // #9CA3AF - мягкий серый
            }
        } else {
            // Light theme colors (можно добавить позже)
            if (isPrimary) {
                0xFF0D1025.toInt() // Тёмный текст для светлой темы
            } else {
                0xFF6B7280.toInt() // Серый текст для светлой темы
            }
        }
    }
    
    /**
     * A7.9: Получить акцентный цвет для тёмной темы
     * Глубокий, насыщенный, контрастный цвет (#3A4DFF)
     */
    fun getAccentColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            DarkTheme.ACCENT // A7.9: #3A4DFF
        } else {
            Accent.GRADIENT_START // #4C6CFF для светлой темы
        }
    }
    
    /**
     * A7.10: Получить отступы для максимальной читаемости
     * Большие отступы для комфортного чтения
     */
    fun getReadablePadding(context: Context): Float {
        return Readability.PADDING_LARGE_DP // A7.10: 20dp для больших отступов
    }
    
    /**
     * A7.10: Получить размер текста для максимальной разборчивости
     */
    fun getReadableTextSize(context: Context): Float {
        return Readability.TEXT_READABLE_SIZE_SP // A7.10: 16sp для оптимальной читаемости
    }
}

