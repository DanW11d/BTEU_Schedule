package com.example.bteu_schedule.utils

import android.util.Log
import com.example.bteu_schedule.data.config.AppConfig

/**
 * Wrapper для логирования с условной логикой
 * 
 * Автоматически отключает логирование в release сборке, если не включено явно в AppConfig
 * 
 * Использование:
 * ```
 * AppLogger.d("Tag", "Message")
 * AppLogger.e("Tag", "Error message", exception)
 * AppLogger.w("Tag", "Warning message")
 * ```
 */
object AppLogger {

    /**
     * Должно ли логирование быть включено
     * В production: только если явно включено в AppConfig
     */
    private val isLoggingEnabled: Boolean
        get() = AppConfig.LOG_API_REQUESTS || AppConfig.LOG_FTP_DETAILS

    /**
     * Debug лог - только в DEBUG режиме или если явно включено
     */
    fun d(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.d(tag, message)
        }
    }

    /**
     * Debug лог с исключением
     */
    fun d(tag: String, message: String, throwable: Throwable?) {
        if (isLoggingEnabled) {
            Log.d(tag, message, throwable)
        }
    }

    /**
     * Error лог - всегда логируется (критические ошибки)
     */
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    /**
     * Error лог с исключением - всегда логируется (критические ошибки)
     */
    fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }

    /**
     * Warning лог - всегда логируется (важные предупреждения)
     */
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    /**
     * Warning лог с исключением - всегда логируется
     */
    fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    /**
     * Info лог - только в DEBUG режиме или если явно включено
     */
    fun i(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.i(tag, message)
        }
    }

    /**
     * Verbose лог - только если логирование включено
     */
    fun v(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.v(tag, message)
        }
    }

    /**
     * Детальный debug лог с разделителями (только если логирование включено)
     * Используется для отладки сложных процессов
     */
    fun debugSection(tag: String, title: String, block: () -> Unit) {
        if (isLoggingEnabled) {
            d(tag, "═══════════════════════════════════════")
            d(tag, title)
            d(tag, "═══════════════════════════════════════")
            block()
            d(tag, "═══════════════════════════════════════")
        }
    }
}

