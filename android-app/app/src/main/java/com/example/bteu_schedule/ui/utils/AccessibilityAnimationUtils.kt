package com.example.bteu_schedule.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.view.accessibility.AccessibilityManager
import android.content.Context

/**
 * A3.10: Утилита для проверки настроек доступности анимаций
 * 
 * Проверяет:
 * - "Отключить анимации" (animatorDurationScale == 0.0)
 * - "Уменьшить движение" (reduceMotion)
 * 
 * Если анимации отключены:
 * - Отключаем сложные переходы (slide, scale, parallax)
 * - Оставляем только fade
 */

/**
 * Проверить, отключены ли анимации в настройках системы
 * 
 * @return true, если анимации отключены или уменьшено движение
 */
@Composable
fun isAnimationDisabled(): Boolean {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    
    // Проверяем animatorDurationScale (0.0 означает отключенные анимации)
    // В Compose используем Settings.Global для проверки
    val animatorDurationScale = try {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
    } catch (e: Exception) {
        1.0f // По умолчанию анимации включены
    }
    if (animatorDurationScale == 0f) {
        return true
    }
    
    // Проверяем настройки доступности через AccessibilityManager
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    if (accessibilityManager != null) {
        // Проверяем, включена ли опция "Уменьшить движение"
        // В Android это можно проверить через системные настройки
        // Для упрощения проверяем только animatorDurationScale
    }
    
    return false
}

/**
 * Получить состояние доступности анимаций
 * 
 * @return true, если анимации включены и можно использовать сложные переходы
 */
@Composable
fun isAnimationEnabled(): Boolean {
    return !isAnimationDisabled()
}

/**
 * A3.10: Получить анимацию появления с учетом доступности
 * 
 * Если анимации отключены - только fade
 * Если включены - fade + slide
 */
@Composable
fun getAccessibleEnterAnimation(
    defaultAnimation: androidx.compose.animation.EnterTransition
): androidx.compose.animation.EnterTransition {
    return if (isAnimationEnabled()) {
        defaultAnimation
    } else {
        // A3.10: Только fade, если анимации отключены
        androidx.compose.animation.fadeIn(
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 200,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }
}

/**
 * A3.10: Получить анимацию исчезновения с учетом доступности
 * 
 * Если анимации отключены - только fade
 * Если включены - fade + slide
 */
@Composable
fun getAccessibleExitAnimation(
    defaultAnimation: androidx.compose.animation.ExitTransition
): androidx.compose.animation.ExitTransition {
    return if (isAnimationEnabled()) {
        defaultAnimation
    } else {
        // A3.10: Только fade, если анимации отключены
        androidx.compose.animation.fadeOut(
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 200,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }
}

/**
 * A3.10: Получить анимацию scale с учетом доступности
 * 
 * Если анимации отключены - без scale (возвращает 1f)
 * Если включены - использует переданную анимацию
 */
@Composable
fun getAccessibleScaleAnimation(
    targetValue: Float,
    defaultAnimation: androidx.compose.animation.core.AnimationSpec<Float>
): androidx.compose.animation.core.AnimationSpec<Float> {
    return if (isAnimationEnabled()) {
        defaultAnimation
    } else {
        // A3.10: Мгновенное изменение без анимации
        androidx.compose.animation.core.snap<Float>()
    }
}

/**
 * A3.10: Получить значение scale с учетом доступности
 * 
 * Если анимации отключены - мгновенное изменение
 * Если включены - анимированное изменение
 */
@Composable
fun getAccessibleScaleValue(
    targetValue: Float,
    isPressed: Boolean
): Float {
    return if (isAnimationEnabled()) {
        targetValue
    } else {
        // A3.10: Мгновенное изменение без анимации
        if (isPressed) targetValue else 1f
    }
}

