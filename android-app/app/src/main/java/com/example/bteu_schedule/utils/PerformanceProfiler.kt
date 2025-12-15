package com.example.bteu_schedule.utils

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.system.measureTimeMillis

/**
 * Утилита для профилирования и мониторинга производительности приложения
 * 
 * Предоставляет инструменты для:
 * - Измерения времени выполнения операций
 * - Мониторинга производительности в debug режиме
 * - Настройки StrictMode для обнаружения проблем производительности
 */
object PerformanceProfiler {
    
    /**
     * Включить StrictMode для обнаружения проблем производительности
     * 
     * StrictMode поможет обнаружить:
     * - Выполнение операций в главном потоке
     * - Утечки ресурсов
     * - Неоптимальные операции с диском/сетью
     * 
     * ВАЖНО: Включать только в debug сборке!
     */
    fun enableStrictMode() {
        // StrictMode включен для профилирования
        // В production будет автоматически отключен, если логирование отключено
        try {
            // Политика для потоков: обнаруживать долгие операции в главном потоке
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectAll() // Обнаруживать все виды проблем
                    .penaltyLog() // Логировать нарушения
                    .penaltyFlashScreen() // Мигать экраном при нарушении
                    .build()
            )
            
            // Политика для VM: обнаруживать утечки ресурсов
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectActivityLeaks() // Утечки Activity
                    .detectLeakedClosableObjects() // Утечки Closable объектов
                    .detectLeakedRegistrationObjects() // Утечки регистраций (BroadcastReceiver, etc.)
                    .detectLeakedSqlLiteObjects() // Утечки SQLite объектов
                    .penaltyLog() // Логировать нарушения
                    .build()
            )
            
            AppLogger.d("PerformanceProfiler", "StrictMode включен для профилирования")
        } catch (e: Exception) {
            // Игнорируем ошибки в production
            AppLogger.w("PerformanceProfiler", "Не удалось включить StrictMode", e)
        }
    }
    
    /**
     * Измерить время выполнения операции
     * 
     * @param tag Тег для логирования
     * @param operation Операция для измерения
     * @return Результат операции
     */
    inline fun <T> measureTime(tag: String, operation: () -> T): T {
        var result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        AppLogger.d("PerformanceProfiler", "$tag выполнена за ${duration}ms")
        return result
    }
    
    /**
     * Измерить время выполнения suspend операции
     * 
     * @param tag Тег для логирования
     * @param operation Suspend операция для измерения
     * @return Результат операции
     */
    suspend inline fun <T> measureTimeSuspend(tag: String, operation: suspend () -> T): T {
        var result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        AppLogger.d("PerformanceProfiler", "$tag выполнена за ${duration}ms")
        return result
    }
    
    /**
     * Обернуть Flow для мониторинга времени первого элемента
     * 
     * @param tag Тег для логирования
     * @param flow Flow для мониторинга
     * @return Flow с мониторингом
     */
    fun <T> monitorFlow(tag: String, flow: Flow<T>): Flow<T> {
        return flow {
            val startTime = System.currentTimeMillis()
            var firstEmitted = false
            flow.collect { value ->
                if (!firstEmitted) {
                    val duration = System.currentTimeMillis() - startTime
                    AppLogger.d("PerformanceProfiler", "$tag: первый элемент за ${duration}ms")
                    firstEmitted = true
                }
                emit(value)
            }
        }
    }
    
    /**
     * Измерить время выполнения и записать в лог с деталями
     * 
     * @param tag Тег для логирования
     * @param details Дополнительные детали
     * @param operation Операция для измерения
     * @return Результат операции
     */
    inline fun <T> measureTimeDetailed(
        tag: String,
        details: String = "",
        operation: () -> T
    ): T {
        var result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        val message = if (details.isNotEmpty()) {
            "$tag ($details) выполнена за ${duration}ms"
        } else {
            "$tag выполнена за ${duration}ms"
        }
        AppLogger.d("PerformanceProfiler", message)
        return result
    }
    
    /**
     * Проверить, не выполняется ли операция слишком долго
     * 
     * @param tag Тег для логирования
     * @param maxDurationMs Максимальная длительность в миллисекундах
     * @param operation Операция для проверки
     * @return Результат операции
     */
    inline fun <T> checkDuration(
        tag: String,
        maxDurationMs: Long,
        operation: () -> T
    ): T {
        var result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        if (duration > maxDurationMs) {
            AppLogger.w("PerformanceProfiler", "⚠️ $tag выполнена за ${duration}ms, превышает лимит ${maxDurationMs}ms")
        } else {
            AppLogger.d("PerformanceProfiler", "$tag выполнена за ${duration}ms")
        }
        return result
    }
}

/**
 * Extension функция для измерения времени выполнения suspend функции
 */
suspend inline fun <T> String.measureTime(operation: suspend () -> T): T {
    return PerformanceProfiler.measureTimeSuspend(this, operation)
}

/**
 * Extension функция для измерения времени выполнения обычной функции
 */
inline fun <T> String.measureTime(operation: () -> T): T {
    return PerformanceProfiler.measureTime(this, operation)
}

