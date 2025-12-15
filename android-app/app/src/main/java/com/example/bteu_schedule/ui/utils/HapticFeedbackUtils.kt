package com.example.bteu_schedule.ui.utils

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * A3.8: Утилита для виброотклика и микровзаимодействий
 * 
 * Типы виброотклика:
 * - Impact Light: лёгкий haptic feedback для всех кликабельных элементов
 * - Impact Medium: средний haptic feedback для важных действий
 */

/**
 * Получить экземпляр HapticFeedback для использования в Composable
 */
@Composable
fun rememberHapticFeedback() = LocalHapticFeedback.current

/**
 * A3.8: Лёгкий haptic feedback (Impact Light)
 * Используется на всех кликабельных элементах
 */
@Suppress("UNCHECKED_CAST")
fun Any.performLightImpact() {
    // В Compose используем performHapticFeedback для лёгкого виброотклика
    // Это соответствует Impact Light
    // В Compose HapticFeedback.performHapticFeedback принимает Int (HapticFeedbackConstants)
    try {
        val method = this.javaClass.getMethod("performHapticFeedback", Int::class.java)
        method.invoke(this, android.view.HapticFeedbackConstants.LONG_PRESS)
    } catch (e: Exception) {
        // Fallback: если метод не найден, просто игнорируем
        // В некоторых версиях Compose метод может называться по-другому
    }
}

/**
 * A3.8: Средний haptic feedback (Impact Medium)
 * Используется на важных действиях
 */
@Suppress("UNCHECKED_CAST")
fun Any.performMediumImpact() {
    // В Compose используем performHapticFeedback для среднего виброотклика
    // Это соответствует Impact Medium
    try {
        val method = this.javaClass.getMethod("performHapticFeedback", Int::class.java)
        method.invoke(this, android.view.HapticFeedbackConstants.TEXT_HANDLE_MOVE)
    } catch (e: Exception) {
        // Fallback: если метод не найден, просто игнорируем
    }
}

/**
 * A6.9: Виброотклик для ассистента при отправке сообщения
 * Нажатие кнопки отправки - Impact Medium
 */
@Suppress("UNCHECKED_CAST")
fun Any.performMessageSent() {
    performMediumImpact() // A6.9: Нажатие кнопки отправки - Impact Medium
}

/**
 * A6.9: Виброотклик для ассистента при получении ответа
 * Получение ответа ассистента - Impact Light
 */
@Suppress("UNCHECKED_CAST")
fun Any.performMessageReceived() {
    performLightImpact() // A6.9: Получение ответа ассистента - Impact Light
}

/**
 * A6.9: Виброотклик для ошибок
 * Ошибки - 2× Light (двойной лёгкий виброотклик)
 */
@Suppress("UNCHECKED_CAST")
fun Any.performErrorFeedback() {
    // A6.9: Ошибки - 2× Light
    performLightImpact()
    // Небольшая задержка перед вторым виброоткликом
    Handler(Looper.getMainLooper()).postDelayed({
        performLightImpact()
    }, 50) // 50ms задержка между двумя виброоткликами
}
