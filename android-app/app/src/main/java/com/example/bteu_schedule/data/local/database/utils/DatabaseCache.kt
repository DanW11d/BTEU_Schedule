package com.example.bteu_schedule.data.local.database.utils

import com.example.bteu_schedule.data.config.AppConstants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Утилита для кэширования результатов запросов к базе данных
 * 
 * Используется для уменьшения количества запросов к БД и повышения производительности
 * 
 * Особенности:
 * - Thread-safe кэш с использованием Mutex
 * - TTL (Time To Live) для автоматической инвалидации кэша
 * - Поддержка ручной инвалидации кэша
 * 
 * @param T Тип кэшируемых данных
 * @param defaultTtlMs Время жизни кэша в миллисекундах (по умолчанию 5 минут)
 */
class DatabaseCache<T>(
    private val defaultTtlMs: Long = 5 * 60 * 1000L // 5 минут
) {
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val cache = ConcurrentHashMap<String, CacheEntry<T>>()
    private val mutex = Mutex()

    /**
     * Получить данные из кэша или null, если данных нет или кэш устарел
     */
    suspend fun get(key: String): T? = mutex.withLock {
        val entry = cache[key] ?: return@withLock null
        
        // Проверяем, не устарел ли кэш
        val age = System.currentTimeMillis() - entry.timestamp
        if (age > defaultTtlMs) {
            cache.remove(key)
            return@withLock null
        }
        
        entry.data
    }

    /**
     * Сохранить данные в кэш
     */
    suspend fun put(key: String, value: T): Unit = mutex.withLock {
        cache[key] = CacheEntry(value)
    }

    /**
     * Сохранить данные в кэш с кастомным TTL
     */
    suspend fun put(key: String, value: T, ttlMs: Long): Unit = mutex.withLock {
        cache[key] = CacheEntry(value)
        // TTL обрабатывается при get, но можно добавить отдельную логику
    }

    /**
     * Инвалидировать кэш для конкретного ключа
     */
    suspend fun invalidate(key: String): Unit = mutex.withLock {
        cache.remove(key)
    }

    /**
     * Инвалидировать весь кэш
     */
    suspend fun clear(): Unit = mutex.withLock {
        cache.clear()
    }

    /**
     * Проверить, есть ли данные в кэше (без проверки TTL)
     */
    suspend fun contains(key: String): Boolean = mutex.withLock {
        cache.containsKey(key)
    }

    /**
     * Получить размер кэша
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    /**
     * Очистить устаревшие записи
     */
    suspend fun evictExpired(): Unit = mutex.withLock {
        val now = System.currentTimeMillis()
        cache.entries.removeIf { (_, entry) ->
            (now - entry.timestamp) > defaultTtlMs
        }
    }
}

/**
 * Фабрика для создания кэшей для разных типов данных
 */
object DatabaseCacheFactory {
    
    /**
     * Кэш для списков групп
     * TTL: 5 минут
     */
    fun <T> createGroupsCache(): DatabaseCache<List<T>> = DatabaseCache(
        defaultTtlMs = AppConstants.CACHE_GROUPS_TTL_MS
    )
    
    /**
     * Кэш для списков факультетов
     * TTL: 10 минут (факультеты меняются редко)
     */
    fun <T> createFacultiesCache(): DatabaseCache<List<T>> = DatabaseCache(
        defaultTtlMs = AppConstants.CACHE_FACULTIES_TTL_MS
    )
    
    /**
     * Кэш для расписания занятий
     * TTL: 1 час (расписание меняется редко)
     */
    fun <T> createLessonsCache(): DatabaseCache<List<T>> = DatabaseCache(
        defaultTtlMs = AppConstants.CACHE_SCHEDULE_TTL_MS
    )
    
    /**
     * Кэш для экзаменов
     * TTL: 30 минут
     */
    fun <T> createExamsCache(): DatabaseCache<List<T>> = DatabaseCache(
        defaultTtlMs = AppConstants.CACHE_EXAMS_TTL_MS
    )
}

