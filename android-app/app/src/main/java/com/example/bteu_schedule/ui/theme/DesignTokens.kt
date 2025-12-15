package com.example.bteu_schedule.ui.theme

import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Дизайн-система: Цветовые токены
 * 
 * Единая цветовая система для светлой и тёмной темы
 * 
 * Тёмная тема
 */
object DarkColors {
    // Основной фон приложения: #000013 - #000018
    val BG = Color(0xFF000013)
    val BGVariant = Color(0xFF000018)
    
    // Фон карточек: #0C0F25
    val Surface = Color(0xFF0C0F25)
    val SurfaceAlt = Color(0xFF0C0F25)
    
    // Акцент: градиент Start: #3A4DFF, End: #000064
    val Primary = Color(0xFF000064) // Primary Base: deep navy blue
    val PrimaryLight = Color(0xFF3A4DFF) // Primary Light 1
    val PrimaryLight2 = Color(0xFF4C6CFF) // Primary Light 2
    val PrimaryPastel = Color(0xFF7A8BFF) // Primary Pastel
    
    // Фирменный градиент (основной)
    val PrimaryGradientStart = Color(0xFF3A4DFF)
    val PrimaryGradientMid = Color(0xFF4C6CFF)
    val PrimaryGradientEnd = Color(0xFF000064)
    
    // Градиенты для карточек
    val ClassicBlueStart = Color(0xFF3A4DFF)
    val ClassicBlueEnd = Color(0xFF000064)
    
    val BrightBlueStart = Color(0xFF4C6CFF)
    val BrightBlueEnd = Color(0xFF000064)
    
    val SoftBlueStart = Color(0xFF7A8BFF)
    val SoftBlueEnd = Color(0xFF000064)
    
    val Secondary = Color(0xFF3A4DFF)
    
    // Текст
    val TextPrimary = Color(0xFFFFFFFF) // Primary: #FFFFFF
    val TextSecondary = Color(0xFFC7CBE1) // Secondary: #C7CBE1
    val TextTertiary = Color(0xFF7F8499) // Tertiary: #7F8499
    val TextOnPrimary = Color(0xFFFFFFFF)
    
    val Border = Color(0xFF23273A)
    val InputBorder = Color(0xFF374151)
    val ChipBG = Color(0xFF1F2937)
    val ChipActiveBG = Color(0xFF3A4DFF) // Используем PrimaryLight для активных элементов
    val Error = Color(0xFFF97373)
}

/**
 * Design Tokens: Светлая тема
 * 
 * Цвета согласно Design Tokens:
 * - --color-primary-500: #3657FF (основной бренд)
 * - --surface-0: #F4F8FF (основной фон приложения)
 * - --surface-1: #FFFFFF (карточки)
 * - --surface-2: #F0F4FF (лёгкие поверхности)
 * - --text-primary: #0D1333 (заголовки)
 * - --text-secondary: #6B7280 (вспомогательный)
 * - --muted: #9AA0AB
 * - --border: rgba(13,19,51,0.06)
 */
object LightColors {
    // Design Tokens: Основной фон приложения
    val BG = Color(0xFFF4F8FF) // --surface-0: #F4F8FF
    val BGVariant = Color(0xFFF0F4FF) // --surface-2: #F0F4FF
    
    // Design Tokens: Фон карточек - светлые поверхности
    val Surface = Color(0xFFFFFFFF) // --surface-1: #FFFFFF
    val SurfaceAlt = Color(0xFFF0F4FF) // --surface-2: #F0F4FF
    
    // Design Tokens: Акцент - основной бренд
    val Primary = Color(0xFF3657FF) // --color-primary-500: #3657FF
    val PrimaryLight = Color(0xFF6FA0FF) // --color-primary-100: #6FA0FF
    val PrimaryLight2 = Color(0xFF5B73FF) // --color-accent: #5B73FF
    val PrimaryDark = Color(0xFF2637C8) // --color-primary-700: #2637C8
    val PrimaryPastel = Color(0xFF7A8BFF) // Для совместимости
    
    // Design Tokens: Фирменный градиент (только для header)
    val PrimaryGradientStart = Color(0xFF3657FF) // --color-primary-500
    val PrimaryGradientMid = Color(0xFF5B73FF) // --color-accent
    val PrimaryGradientEnd = Color(0xFF2637C8) // --color-primary-700
    
    // Градиенты для карточек (для совместимости, но карточки теперь светлые)
    val ClassicBlueStart = Color(0xFF3657FF)
    val ClassicBlueEnd = Color(0xFF2637C8)
    
    val BrightBlueStart = Color(0xFF5B73FF)
    val BrightBlueEnd = Color(0xFF3657FF)
    
    val SoftBlueStart = Color(0xFF6FA0FF)
    val SoftBlueEnd = Color(0xFF3657FF)
    
    val Secondary = Color(0xFF5B73FF) // --color-accent
    
    // Design Tokens: Текст
    val TextPrimary = Color(0xFF0D1333) // --text-primary: #0D1333 (заголовки)
    val TextSecondary = Color(0xFF6B7280) // --text-secondary: #6B7280 (вспомогательный)
    val TextTertiary = Color(0xFF9AA0AB) // --muted: #9AA0AB
    val TextOnPrimary = Color(0xFFFFFFFF)
    
    // Design Tokens: Границы и элементы
    val Border = Color(0x0F0D1333) // --border: rgba(13,19,51,0.06)
    val InputBorder = Color(0xFFD1D5DB) // Для совместимости
    val ChipBG = Color(0xFFF0F4FF) // --surface-2
    val ChipActiveBG = Color(0xFF3657FF) // --color-primary-500
    
    // Design Tokens: Статусы
    val Success = Color(0xFF26C281) // --color-success: #26C281
    val Warning = Color(0xFFFFB020) // --color-warning: #FFB020
    val Error = Color(0xFFFF4D4F) // --color-error: #FF4D4F
}

/**
 * Design Tokens: Радиусы, тени, бордеры
 * 
 * --radius-sm: 8px
 * --radius-md: 12px (основной для карточек)
 * --radius-lg: 16px
 * --shadow-1: 0 6px 12px rgba(13,19,51,0.06)
 * --shadow-2: 0 10px 30px rgba(13,19,51,0.08)
 * --border-width: 1px
 */
object DesignRadius {
    val XS = 8.dp     // --radius-sm: 8px
    val S = 12.dp     // --radius-md: 12px (основной для карточек согласно Design Tokens)
    val M = 16.dp     // --radius-lg: 16px
    val L = 24.dp     // Для совместимости (большие карточки)
    val XL = 32.dp    // Для совместимости
    
    // Специальные значения
    val ListCard = 12.dp // Design Tokens: --radius-md для карточек (8-12dp согласно рекомендациям)
    val Full = 999.dp    // Radius-Full: круглые элементы (иконки в кругах)
}

/**
 * Design Tokens: Spacing (spacing scale)
 * 
 * Использовать единый scale — кратные 4px:
 * - --space-0: 4px
 * - --space-1: 8px
 * - --space-2: 12px
 * - --space-3: 16px
 * - --space-4: 20px
 * - --space-5: 24px
 * - --space-6: 32px
 * - --space-7: 40px
 * 
 * Применение: карточка — padding: var(--space-2) var(--space-3); между карточками gap var(--space-3)
 */
object DesignSpacing {
    // Design Tokens: Spacing scale (кратные 4px)
    val XS = 4.dp      // --space-0: 4px
    val S = 8.dp       // --space-1: 8px
    val M = 12.dp      // --space-2: 12px
    val Base = 16.dp   // --space-3: 16px
    val L = 20.dp      // --space-4: 20px
    val XL = 24.dp     // --space-5: 24px
    val XXL = 32.dp    // --space-6: 32px
    val XXXL = 40.dp   // --space-7: 40px
    
    // Design Tokens: Карточки - padding: 12px 16px (vertical 12px, horizontal 16px)
    val CardPaddingVertical = 12.dp   // --space-2: 12px (vertical padding для карточек)
    val CardPaddingHorizontal = 16.dp // --space-3: 16px (horizontal padding для карточек)
    
    // Обратная совместимость
    val CardPadding = 16.dp        // Для совместимости (используйте CardPaddingHorizontal)
    val CardPaddingLarge = 20.dp   // Для совместимости
    val BetweenBlocks = 16.dp       // Для совместимости
    val FromHeader = 24.dp          // Для совместимости
    val CardInternalSpacing = 8.dp  // Для совместимости
    val CardInternalSpacingLarge = 12.dp // Для совместимости
    val IconTextSpacing = 12.dp     // Для совместимости
    val CardRowSpacing = 8.dp      // Для совместимости
    
    // Специальные размеры компонентов
    val ButtonHeight = 48.dp       // Высота кнопок
    val SearchFieldHeight = 48.dp  // Высота поля поиска
}

/**
 * Дизайн-система: Высота компонентов
 */
object DesignHeights {
    val Button = 48.dp
    val SearchField = 48.dp
    // A4.2: Высота панели навигации: 84dp (включая safe-area снизу)
    val BottomNavigation = 84.dp
    val HeaderDefault = 108.dp    // Общая высота хедера с градиентом: 104-112dp (используем 108dp)
    val HeaderCollapsed = 70.dp   // Высота хедера при скролле (не используется для фиксированного хедера)
    val HeaderFadeHeight = 36.dp  // Высота fade-перехода: 32-40dp (узкая полоска)
}

/**
 * Дизайн-система: Тени (Elevation) — A3.5
 * 
 * Правила визуальной иерархии (A3.5):
 * - Тень мягкая, а не "грязная"
 * - Использовать DesignShadows.Low для карточек (мягкая тень)
 * - Избегать резких, "грязных" теней
 * 
 * Light theme:
 * - ShadowLow: 4dp blur, 5% black (мягкая тень)
 * - ShadowMid: 8dp blur, 8% black
 * - ShadowHigh: 12dp blur, 10% black
 * 
 * Dark theme:
 * - ShadowLow: 4dp blur, 40% blue tint (мягкая тень)
 * - ShadowMid: 8dp blur, 50% blue tint
 * - ShadowHigh: 12dp blur, 60% blue tint
 */
object DesignShadows {
    /**
     * ShadowLow: 4dp blur, 5% black (Light) / 40% blue tint (Dark)
     * Используется для легких поднятий элементов
     */
    val Low = ShadowSpec(
        elevation = 4.dp,
        lightOpacity = 0.05f,
        darkOpacity = 0.40f
    )
    
    /**
     * ShadowMid: 8dp blur, 8% black (Light) / 50% blue tint (Dark)
     * Используется для средних поднятий (карточки, кнопки)
     */
    val Mid = ShadowSpec(
        elevation = 8.dp,
        lightOpacity = 0.08f,
        darkOpacity = 0.50f
    )
    
    /**
     * ShadowHigh: 12dp blur, 10% black (Light) / 60% blue tint (Dark)
     * Используется для высоких поднятий (модальные окна, важные карточки)
     */
    val High = ShadowSpec(
        elevation = 12.dp,
        lightOpacity = 0.10f,
        darkOpacity = 0.60f
    )
    
    /**
     * Спецификация тени
     */
    data class ShadowSpec(
        val elevation: androidx.compose.ui.unit.Dp,
        val lightOpacity: Float,
        val darkOpacity: Float
    ) {
        /**
         * Получить цвет тени для Light theme (черный)
         */
        fun shadowColorLight(): androidx.compose.ui.graphics.Color {
            return androidx.compose.ui.graphics.Color.Black.copy(alpha = lightOpacity)
        }
        
        /**
         * Получить цвет тени для Dark theme (синий оттенок)
         * Используется PrimaryGradientStart (#3A4DFF) как базовый синий цвет
         */
        fun shadowColorDark(): androidx.compose.ui.graphics.Color {
            return DarkColors.PrimaryGradientStart.copy(alpha = darkOpacity)
        }
        
        /**
         * Получить цвет тени в зависимости от темы
         * @param isDarkTheme true для Dark theme, false для Light theme
         */
        fun shadowColor(isDarkTheme: Boolean): androidx.compose.ui.graphics.Color {
            return if (isDarkTheme) {
                shadowColorDark()
            } else {
                shadowColorLight()
            }
        }
    }
}

/**
 * Helper функция для применения тени к Modifier
 * Автоматически определяет тему и применяет правильный цвет тени
 * 
 * @param shadowSpec Спецификация тени (DesignShadows.Low, Mid, High)
 * @param shape Форма элемента (RoundedCornerShape)
 * @param isDarkTheme Опционально: явно указать тему. Если не указано, определяется автоматически
 * @return Modifier с примененной тенью
 */
@Composable
fun Modifier.applyShadow(
    shadowSpec: DesignShadows.ShadowSpec,
    shape: Shape,
    isDarkTheme: Boolean? = null
): Modifier {
    val isDark = isDarkTheme ?: (MaterialTheme.colorScheme.background == DarkColors.BG)
    val shadowColor = shadowSpec.shadowColor(isDark)
    return this.shadow(
        elevation = shadowSpec.elevation,
        shape = shape,
        spotColor = shadowColor,
        ambientColor = shadowColor
    )
}

/**
 * Дизайн-система: Размеры иконок (A3.4)
 * 
 * Правила:
 * - Минимальный размер: 20dp
 * - Основной размер: 24dp
 * - Большие иконки (в карточках): 32dp
 * - Все иконки выровнены по центру контейнера
 * 
 * Цвета:
 * - Primary карточки: Белая (#FFFFFF)
 * - Списки/настройки: #4C6CFF (light), #7A8BFF (dark)
 * - Неактивные: #9CA3AF
 */
object DesignIconSizes {
    val Small = 20.dp   // Минимальный размер (A3.4)
    val Medium = 24.dp  // Основной размер (A3.4)
    val Large = 32.dp   // Большие иконки в карточках (A3.4)
    val IconButtonSize = 48.dp    // Размер кнопки с иконкой (A3.3)
}
