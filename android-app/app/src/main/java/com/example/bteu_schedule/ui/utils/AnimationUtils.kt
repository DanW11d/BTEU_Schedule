package com.example.bteu_schedule.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignHeights

/**
 * A6. Премиальная система анимаций (Motion System)
 * 
 * Премиальная система анимаций в стиле Apple / флагманский Android
 * Все анимации оптимизированы для 60fps, плавные и тактильные
 * 
 * Использует MotionTokens для единообразия (A6.1)
 */

import com.example.bteu_schedule.ui.theme.MotionDuration
import com.example.bteu_schedule.ui.theme.MotionEasing
import com.example.bteu_schedule.ui.theme.MotionSpecs

// ========== БАЗОВЫЕ ПАРАМЕТРЫ АНИМАЦИЙ ==========
// A6.1: Используем токены из MotionTokens

/**
 * Стандартная длительность для базовых анимаций
 * A6.1: Используем MotionDuration.Medium (200ms)
 */
private const val STANDARD_DURATION = MotionDuration.Medium

/**
 * Быстрая длительность для микроанимаций
 * A6.1: Используем MotionDuration.Fast (120ms)
 */
private const val FAST_DURATION = MotionDuration.Fast

/**
 * Медленная длительность для переходов
 * A6.1: Используем MotionDuration.Slow (280ms)
 */
private const val SLOW_DURATION = MotionDuration.Slow

/**
 * Стандартная spring конфигурация (лёгкий spring без перебора)
 * A6.1: Используем MotionEasing.SpringSmooth
 */
private val standardSpring = MotionEasing.SpringSmooth

/**
 * Быстрая spring конфигурация для тактильных откликов
 * A6.1: Используем MotionEasing.SpringFast
 */
private val fastSpring = MotionEasing.SpringFast

// ========== АНИМАЦИИ КАРТОЧЕК ==========

/**
 * Премиальная анимация нажатия на карточку
 * scale: 1.0 → 0.97 → 1.0 (150ms)
 * тень усиливается, имитируя физическое нажатие
 */
@Composable
fun Modifier.premiumCardPressAnimation(
    isPressed: Boolean
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressedState) 0.97f else 1f,
        animationSpec = MotionSpecs.fastSpring(), // A6.1: Используем Spring для кликов/микроанимаций
        label = "cardPress"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressedState) 0.9f else 1f,
        animationSpec = tween(
            durationMillis = MotionDuration.Fast, // A6.1: Используем MotionDuration.Fast (120ms)
            easing = MotionEasing.EaseOutCubic // A6.1: Используем EaseOutCubic для плавных переходов
        ),
        label = "cardAlpha"
    )
    
    this
        .scale(scale)
        .alpha(alpha)
}

/**
 * A6.3.1: Staggered анимация появления карточек (список факультетов / курсов / групп)
 * 
 * Каждая карточка появляется как:
 * - translateY(16dp → 0)
 * - opacity (0 → 1)
 * - с задержкой 40ms по цепочке
 * 
 * Это называется staggered list animation.
 */
@Composable
fun premiumCardAppearAnimation(index: Int): EnterTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeIn(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium, // A6.1: Используем MotionDuration.Medium
                delayMillis = index * 40, // A6.3.1: Задержка 40ms по цепочке
                easing = MotionEasing.EaseOutCubic // A6.1: Используем EaseOutCubic для плавных появлений
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 }, // A6.3.1: translateY(16dp → 0) - примерно 1/4 высоты экрана
            animationSpec = tween(
                durationMillis = MotionDuration.Medium, // A6.1: Используем MotionDuration.Medium
                delayMillis = index * 40, // A6.3.1: Задержка 40ms по цепочке
                easing = MotionEasing.EaseOutCubic // A6.1: Используем EaseOutCubic для плавных появлений
            )
        )
    } else {
        // A3.10: Только fade, если анимации отключены
        fadeIn(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

// ========== АНИМАЦИИ ХЕДЕРА ==========

/**
 * Премиальная анимация хедера при скролле
 * высота плавно уменьшается
 * заголовок уменьшается и смещается вверх
 * fade остаётся ровным, без скачков
 * синхронизировано 150-200ms
 */
@Composable
fun premiumHeaderScrollAnimation(
    isScrolled: Boolean
): HeaderAnimationState {
    val height by animateDpAsState(
        targetValue = if (isScrolled) DesignHeights.HeaderCollapsed else DesignHeights.HeaderDefault,
        animationSpec = tween(
            durationMillis = STANDARD_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "headerHeight"
    )
    
    val textScale by animateFloatAsState(
        targetValue = if (isScrolled) 0.88f else 1f,
        animationSpec = tween(
            durationMillis = STANDARD_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "textScale"
    )
    
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.3f else 1f,
        animationSpec = tween(
            durationMillis = STANDARD_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "fadeAlpha"
    )
    
    return remember(isScrolled) {
        HeaderAnimationState(
            height = height,
            textScale = textScale,
            fadeAlpha = fadeAlpha
        )
    }
}

data class HeaderAnimationState(
    val height: androidx.compose.ui.unit.Dp,
    val textScale: Float,
    val fadeAlpha: Float
)

// ========== АНИМАЦИИ КНОПОК ==========

/**
 * Премиальная анимация кнопки при нажатии
 * фон темнеет на 10-15%
 * кнопка сжимается до 0.97 и отпрыгивает обратно
 * тень становится чуть сильнее
 */
@Composable
fun premiumButtonPressAnimation(
    isPressed: Boolean
): ButtonAnimationState {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = fastSpring,
        label = "buttonPress"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 4f,
        animationSpec = tween(
            durationMillis = FAST_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "buttonElevation"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(
            durationMillis = FAST_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "buttonAlpha"
    )
    
    return remember(isPressed) {
        ButtonAnimationState(
            scale = scale,
            elevation = elevation,
            alpha = alpha
        )
    }
}

data class ButtonAnimationState(
    val scale: Float,
    val elevation: Float,
    val alpha: Float = 1f
)

/**
 * Анимация включения/отключения кнопки
 * плавный переход цвета и прозрачности за 150-180ms
 */
@Composable
fun premiumButtonEnabledAnimation(
    enabled: Boolean
): ButtonEnabledState {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        ),
        label = "buttonEnabled"
    )
    
    return remember(enabled) {
        ButtonEnabledState(alpha = alpha)
    }
}

data class ButtonEnabledState(
    val alpha: Float
)

// ========== АНИМАЦИИ ЧИПОВ ==========

/**
 * Премиальная анимация чипа фильтра
 * фон плавно меняется серый ↔ синий
 * текст — тёмный ↔ белый
 * при нажатии — лёгкий сдвиг вниз на 1-2px и уменьшение до 0.97
 */
@Composable
fun premiumChipPressAnimation(
    isPressed: Boolean
): ChipAnimationState {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = FAST_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "chipPress"
    )
    
    return remember(isPressed) {
        ChipAnimationState(scale = scale)
    }
}

data class ChipAnimationState(
    val scale: Float
)

// ========== АНИМАЦИИ НАВИГАЦИИ ==========

/**
 * Премиальная анимация нижней навигации
 * круглая активная кнопка расширяется/сжимается (scale 0.9 → 1.0)
 * иконка меняет цвет с плавным переходом
 * при повторном тапе — небольшое подпрыгивание (bounce)
 */
@Composable
fun premiumNavigationAnimation(
    isSelected: Boolean,
    isPressed: Boolean
): NavigationAnimationState {
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isSelected -> 1.0f
            else -> 0.9f
        },
        animationSpec = if (isPressed) {
            fastSpring
        } else {
            standardSpring
        },
        label = "navScale"
    )
    
    return remember(isSelected, isPressed) {
        NavigationAnimationState(scale = scale)
    }
}

data class NavigationAnimationState(
    val scale: Float
)

// ========== ПЕРЕХОДЫ МЕЖДУ ЭКРАНАМИ ==========

/**
 * A6.2.1: Стандартный переход (navigate)
 * 
 * Slide ↔️ Fade
 * Длительность: 220ms
 * Направление: слева → вправо (Android)
 * Элементы анимируются:
 * - Контент смещается на 16dp
 * - Прозрачность: 0 → 1
 */
@Composable
fun premiumScreenEnterAnimation(): EnterTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeIn(
            animationSpec = tween(
                durationMillis = 220, // A6.2.1: Длительность 220ms
                easing = MotionEasing.EaseOutCubic // A6.1: Используем EaseOutCubic
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 4 }, // A6.2.1: Контент смещается на 16dp (примерно 1/4 ширины экрана)
            animationSpec = tween(
                durationMillis = 220, // A6.2.1: Длительность 220ms
                easing = MotionEasing.EaseOutCubic // A6.1: Используем EaseOutCubic
            )
        )
    } else {
        // A3.10: Только fade, если анимации отключены
        fadeIn(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

/**
 * A6.2.1: Стандартный переход (navigate) - Exit
 * 
 * Slide ↔️ Fade (обратный)
 * Длительность: 220ms
 * Направление: вправо → влево
 */
@Composable
fun premiumScreenExitAnimation(): ExitTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeOut(
            animationSpec = tween(
                durationMillis = 220, // A6.2.1: Длительность 220ms
                easing = MotionEasing.EaseInOutCubic // A6.1: Используем EaseInOutCubic
            )
        ) + slideOutHorizontally(
            targetOffsetX = { -it / 4 }, // A6.2.1: Контент смещается на 16dp в обратную сторону (примерно 1/4 ширины экрана)
            animationSpec = tween(
                durationMillis = 220, // A6.2.1: Длительность 220ms
                easing = MotionEasing.EaseInOutCubic // A6.1: Используем EaseInOutCubic
            )
        )
    } else {
        // A3.10: Только fade, если анимации отключены
        fadeOut(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

/**
 * A6.2.2: Возврат назад (pop)
 * 
 * Slide обратный
 * Длительность: 180ms
 * Контент слегка уменьшает высоту (опционально)
 */
@Composable
fun premiumScreenPopEnterAnimation(): EnterTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeIn(
            animationSpec = tween(
                durationMillis = 180, // A6.2.2: Длительность 180ms
                easing = MotionEasing.EaseOutCubic
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it / 4 }, // A6.2.2: Обратное направление (справа налево)
            animationSpec = tween(
                durationMillis = 180, // A6.2.2: Длительность 180ms
                easing = MotionEasing.EaseOutCubic
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

@Composable
fun premiumScreenPopExitAnimation(): ExitTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeOut(
            animationSpec = tween(
                durationMillis = 180, // A6.2.2: Длительность 180ms
                easing = MotionEasing.EaseInOutCubic
            )
        ) + slideOutHorizontally(
            targetOffsetX = { it / 4 }, // A6.2.2: Обратное направление (влево вправо)
            animationSpec = tween(
                durationMillis = 180, // A6.2.2: Длительность 180ms
                easing = MotionEasing.EaseInOutCubic
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

/**
 * A6.2.3: Переход к ассистенту
 * 
 * Ассистент — центральная фича → делаем красивее:
 * - Fade-in
 * - Scale 0.95 → 1.0
 * - duration: 180–220ms
 * - Ощущение, что панель ассистента «выезжает» как окно ChatGPT
 */
@Composable
fun premiumAssistantEnterAnimation(): EnterTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeIn(
            animationSpec = tween(
                durationMillis = 200, // A6.2.3: Длительность 180-220ms (используем 200ms)
                easing = MotionEasing.EaseOutCubic
            )
        ) + slideInVertically(
            initialOffsetY = { (it * 0.05).toInt() }, // A6.2.3: Имитация scale через slide (5% смещения для эффекта "выезжает")
            animationSpec = tween(
                durationMillis = 200, // A6.2.3: Длительность 180-220ms (используем 200ms)
                easing = MotionEasing.EaseOutCubic
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

@Composable
fun premiumAssistantExitAnimation(): ExitTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeOut(
            animationSpec = tween(
                durationMillis = 200,
                easing = MotionEasing.EaseInOutCubic
            )
        ) + slideOutVertically(
            targetOffsetY = { (it * 0.05).toInt() }, // A6.2.3: Имитация scale через slide
            animationSpec = tween(
                durationMillis = 200,
                easing = MotionEasing.EaseInOutCubic
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = MotionDuration.Medium,
                easing = MotionEasing.EaseOutCubic
            )
        )
    }
}

// ========== АНИМАЦИИ СООБЩЕНИЙ (AI ЧАТ) ==========

/**
 * Премиальная анимация появления сообщения
 * сообщения пользователя — справа налево
 * ассистента — слева направо
 * fade + slide
 * A3.10: Если анимации отключены - только fade
 */
@Composable
fun premiumMessageEnterAnimation(isUser: Boolean): EnterTransition {
    val isEnabled = com.example.bteu_schedule.ui.utils.isAnimationEnabled()
    return if (isEnabled) {
        fadeIn(
            animationSpec = tween(
                durationMillis = STANDARD_DURATION,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { if (isUser) it / 2 else -it / 2 },
            animationSpec = tween(
                durationMillis = STANDARD_DURATION,
                easing = FastOutSlowInEasing
            )
        )
    } else {
        // A3.10: Только fade, если анимации отключены
        fadeIn(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        )
    }
}

// ========== АНИМАЦИИ ПОЛЯ ВВОДА ==========

/**
 * Премиальная анимация фокуса поля ввода
 * рамка и тень усиливаются
 * внутри появляется лёгкий glow
 * placeholder плавно сдвигается/уменьшается
 */
@Composable
fun premiumInputFocusAnimation(
    isFocused: Boolean
): InputFocusState {
    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 8f else 2f,
        animationSpec = tween(
            durationMillis = STANDARD_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "inputElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(
            durationMillis = STANDARD_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "inputScale"
    )
    
    return remember(isFocused) {
        InputFocusState(
            elevation = elevation,
            scale = scale
        )
    }
}

data class InputFocusState(
    val elevation: Float,
    val scale: Float
)

// ========== АНИМАЦИЯ СМЕНЫ ТЕМЫ ==========

/**
 * Crossfade при смене темы
 * короткий переход 150-200ms без резкого мигания
 * Возвращает EnterTransition для использования в AnimatedContent
 */
fun premiumThemeTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Exit transition для смены темы
 * короткий переход 150-200ms без резкого мигания
 */
fun premiumThemeExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        )
    )
}

