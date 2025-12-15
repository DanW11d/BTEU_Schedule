package com.example.bteu_schedule.utils

import android.content.Context
import android.os.Debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Метрики производительности приложения
 * 
 * Собирает и предоставляет информацию о:
 * - Использовании памяти
 * - Времени выполнения операций
 * - Производительности БД
 * - Производительности сети
 */
object PerformanceMetrics {
    
    data class MetricsData(
        val memoryUsageMB: Float = 0f,
        val totalMemoryMB: Float = 0f,
        val usedMemoryPercent: Float = 0f,
        val threadCount: Int = 0
    )
    
    private val _metrics = MutableStateFlow(MetricsData())
    val metrics: StateFlow<MetricsData> = _metrics.asStateFlow()
    
    /**
     * Обновить метрики производительности
     */
    fun updateMetrics(context: Context) {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024).toFloat()
            val maxMemory = runtime.maxMemory() / (1024 * 1024).toFloat()
            val usedPercent = (usedMemory / maxMemory) * 100
            
            val threadCount = Thread.activeCount()
            
            _metrics.value = MetricsData(
                memoryUsageMB = usedMemory,
                totalMemoryMB = maxMemory,
                usedMemoryPercent = usedPercent,
                threadCount = threadCount
            )
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }
    
    /**
     * Получить текущее использование памяти в MB
     */
    fun getMemoryUsageMB(): Float {
        return try {
            val runtime = Runtime.getRuntime()
            (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024).toFloat()
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Получить максимальную доступную память в MB
     */
    fun getMaxMemoryMB(): Float {
        return try {
            val runtime = Runtime.getRuntime()
            runtime.maxMemory() / (1024 * 1024).toFloat()
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Получить процент использования памяти
     */
    fun getMemoryUsagePercent(): Float {
        return try {
            val used = getMemoryUsageMB()
            val max = getMaxMemoryMB()
            if (max > 0) (used / max) * 100 else 0f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Получить количество активных потоков
     */
    fun getThreadCount(): Int {
        return try {
            Thread.activeCount()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Проверить, используется ли слишком много памяти
     * 
     * @param threshold Процент использования памяти, при котором выдавать предупреждение (по умолчанию 80%)
     * @return true, если использование памяти превышает порог
     */
    fun isMemoryUsageHigh(threshold: Float = 80f): Boolean {
        return try {
            val usage = getMemoryUsagePercent()
            if (usage > threshold) {
                AppLogger.w(
                    "PerformanceMetrics",
                    "⚠️ Высокое использование памяти: ${usage.toInt()}% (${getMemoryUsageMB().toInt()}MB/${getMaxMemoryMB().toInt()}MB)"
                )
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Логировать текущие метрики
     */
    fun logMetrics(context: String = "App") {
        try {
            val memoryUsage = getMemoryUsageMB()
            val maxMemory = getMaxMemoryMB()
            val memoryPercent = getMemoryUsagePercent()
            val threads = getThreadCount()
            
            AppLogger.d(
                "PerformanceMetrics",
                """
                |═══════════════════════════════════════
                |МЕТРИКИ ПРОИЗВОДИТЕЛЬНОСТИ: $context
                |───────────────────────────────────────
                |Память: ${memoryUsage.toInt()}MB / ${maxMemory.toInt()}MB (${memoryPercent.toInt()}%)
                |Активные потоки: $threads
                |═══════════════════════════════════════
                """.trimMargin()
            )
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }
    
    /**
     * Получить информацию о нативной памяти (требует Debug API)
     */
    fun getNativeHeapSizeMB(): Long {
        return try {
            Debug.getNativeHeapSize() / (1024 * 1024)
        } catch (e: Exception) {
            AppLogger.w("PerformanceMetrics", "Не удалось получить размер нативной кучи", e)
            0L
        }
    }
    
    /**
     * Получить количество выделенной нативной памяти
     */
    fun getNativeHeapAllocatedMB(): Long {
        return try {
            Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
        } catch (e: Exception) {
            AppLogger.w("PerformanceMetrics", "Не удалось получить размер выделенной нативной кучи", e)
            0L
        }
    }
    
    /**
     * Запустить сборку мусора (только для debug, не использовать в production)
     */
    fun forceGarbageCollection() {
        try {
            AppLogger.d("PerformanceMetrics", "Принудительная сборка мусора...")
            val beforeMemory = getMemoryUsageMB()
            System.gc()
            Thread.sleep(100) // Даем время на выполнение GC
            val afterMemory = getMemoryUsageMB()
            val freedMemory = beforeMemory - afterMemory
            AppLogger.d("PerformanceMetrics", "Память до GC: ${beforeMemory.toInt()}MB, после: ${afterMemory.toInt()}MB, освобождено: ${freedMemory.toInt()}MB")
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }
}

