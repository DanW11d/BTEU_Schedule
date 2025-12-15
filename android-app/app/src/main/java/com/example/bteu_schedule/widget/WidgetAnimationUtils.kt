package com.example.bteu_schedule.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * A6.8: Анимации виджетов (Android)
 * A7.8: Премиум-эффекты для виджетов
 * 
 * Виджет тоже может быть «живым»:
 * - появление текста: fade
 * - micro-bounce кнопки «Открыть»
 * - мягкое обновление (когда ассистент сменил реплику)
 * 
 * A7.8. Премиум-эффекты:
 * - Появление: Fade-in 200ms + scale 0.96 → 1.0
 * - Обновление данных: мягкая смена текста (opacity 0 → 1)
 * - Кнопка: bounce scale 0.9 → 1.0
 */

/**
 * A6.8: Появление текста - fade анимация
 * 
 * Параметры:
 * - Duration: 200ms (стандартная для fade)
 * - Easing: DecelerateInterpolator (плавное замедление)
 */
fun View.fadeInText(
    duration: Long = 200,
    startDelay: Long = 0
): Animator {
    alpha = 0f
    visibility = View.VISIBLE
    
    return ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
        this.duration = duration
        this.startDelay = startDelay
        interpolator = DecelerateInterpolator()
    }
}

/**
 * A6.8: Micro-bounce кнопки «Открыть»
 * A7.8: Кнопка: bounce scale 0.9 → 1.0
 * 
 * Параметры:
 * - Scale: 0.9 → 1.0 (A7.8: изменено с 1.0 → 1.1 → 1.0)
 * - Duration: 300ms
 * - Easing: OvershootInterpolator (лёгкий отскок)
 */
fun View.microBounce(
    duration: Long = 300,
    startDelay: Long = 0
): Animator {
    // A7.8: Кнопка: bounce scale 0.9 → 1.0
    val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.9f, 1.0f)
    val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.9f, 1.0f)
    
    return AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        this.duration = duration
        this.startDelay = startDelay
        interpolator = OvershootInterpolator(1.5f) // A6.8: лёгкий отскок
    }
}

/**
 * A6.8: Мягкое обновление (когда ассистент сменил реплику)
 * A7.8: Обновление данных: мягкая смена текста (opacity 0 → 1)
 * 
 * Параметры:
 * - Fade in нового текста: 200ms (A7.8: только opacity 0 → 1, без fade out и translateY)
 * - Duration: 200ms
 * - Easing: DecelerateInterpolator (плавное замедление)
 */
fun View.softUpdate(
    duration: Long = 200, // A7.8: 200ms для мягкой смены текста
    startDelay: Long = 0
): Animator {
    // A7.8: Обновление данных: мягкая смена текста (opacity 0 → 1)
    alpha = 0f
    visibility = View.VISIBLE
    
    return ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
        this.duration = duration
        this.startDelay = startDelay
        interpolator = DecelerateInterpolator() // A7.8: плавное замедление
    }
}

/**
 * A6.8: Комбинированная анимация появления виджета
 * A7.8: Появление: Fade-in 200ms + scale 0.96 → 1.0
 * 
 * Последовательность:
 * 1. Fade in + scale текста (200ms, scale 0.96 → 1.0)
 * 2. Micro-bounce кнопки «Открыть» (300ms, задержка 200ms, scale 0.9 → 1.0)
 */
fun animateWidgetAppearance(
    textView: View,
    buttonView: View,
    totalDuration: Long = 500
): AnimatorSet {
    // A7.8: Появление: Fade-in 200ms + scale 0.96 → 1.0
    textView.alpha = 0f
    textView.scaleX = 0.96f
    textView.scaleY = 0.96f
    textView.visibility = View.VISIBLE
    
    val textFadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
        duration = 200
        interpolator = DecelerateInterpolator()
    }
    
    val textScaleX = ObjectAnimator.ofFloat(textView, "scaleX", 0.96f, 1.0f).apply {
        duration = 200
        interpolator = DecelerateInterpolator()
    }
    
    val textScaleY = ObjectAnimator.ofFloat(textView, "scaleY", 0.96f, 1.0f).apply {
        duration = 200
        interpolator = DecelerateInterpolator()
    }
    
    val textAnimation = AnimatorSet().apply {
        playTogether(textFadeIn, textScaleX, textScaleY)
    }
    
    // A7.8: Кнопка: bounce scale 0.9 → 1.0
    val buttonBounce = buttonView.microBounce(duration = 300, startDelay = 200)
    
    return AnimatorSet().apply {
        playTogether(textAnimation, buttonBounce)
        duration = totalDuration
    }
}

/**
 * A6.8: Анимация обновления виджета ассистента
 * A7.8: Обновление данных: мягкая смена текста (opacity 0 → 1)
 * 
 * Когда ассистент сменил реплику:
 * 1. Мягкое обновление текста (200ms, opacity 0 → 1)
 * 2. Лёгкий bounce кнопки для привлечения внимания (300ms, задержка 200ms, scale 0.9 → 1.0)
 */
fun animateWidgetUpdate(
    textView: View,
    buttonView: View? = null,
    totalDuration: Long = 500 // A7.8: 200ms текст + 300ms кнопка
): AnimatorSet {
    // A7.8: Обновление данных: мягкая смена текста (opacity 0 → 1)
    val textSoftUpdate = textView.softUpdate(duration = 200, startDelay = 0)
    
    val animators = mutableListOf<Animator>(textSoftUpdate)
    
    buttonView?.let {
        // A7.8: Кнопка: bounce scale 0.9 → 1.0
        val buttonBounce = it.microBounce(duration = 300, startDelay = 200)
        animators.add(buttonBounce)
    }
    
    return AnimatorSet().apply {
        playTogether(animators)
        duration = totalDuration
    }
}

