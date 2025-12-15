package com.example.bteu_schedule.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Менеджер для управления автообновлением расписания
 * Обновление происходит раз в неделю после субботы 00:01
 */
class ScheduleUpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ScheduleUpdateManager"
        private const val PREFS_NAME = "schedule_update_prefs"
        private const val KEY_LAST_UPDATE = "last_update_timestamp"
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Получить время последнего обновления
     */
    fun getLastUpdate(): LocalDateTime? {
        val timestamp = prefs.getLong(KEY_LAST_UPDATE, 0L)
        return if (timestamp > 0) {
            LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
        } else {
            null
        }
    }
    
    /**
     * Сохранить время последнего обновления
     */
    fun setLastUpdate(dateTime: LocalDateTime = LocalDateTime.now()) {
        val timestamp = dateTime.atZone(java.time.ZoneId.systemDefault())
            .toEpochSecond() * 1000
        prefs.edit().putLong(KEY_LAST_UPDATE, timestamp).apply()
        Log.d(TAG, "Обновлено время последнего обновления: $dateTime")
    }
    
    /**
     * Вычислить время последней субботы в 00:01
     * Если сейчас суббота после 00:01, возвращает текущую субботу
     * Иначе возвращает прошлую субботу
     */
    fun getLastSaturdayAt001(): LocalDateTime {
        val now = LocalDateTime.now()
        val currentDayOfWeek = now.dayOfWeek
        
        // Вычисляем последнюю субботу
        val daysToSubtract = when (currentDayOfWeek) {
            DayOfWeek.SATURDAY -> {
                // Если сейчас суббота, проверяем время
                if (now.toLocalTime().isAfter(LocalTime.of(0, 1))) {
                    0 // Сегодняшняя суббота после 00:01
                } else {
                    7 // Прошлая суббота
                }
            }
            DayOfWeek.SUNDAY -> 1 // Вчера была суббота
            else -> currentDayOfWeek.value + 1 // Дней до прошлой субботы
        }
        
        val lastSaturday = now.minusDays(daysToSubtract.toLong())
            .with(DayOfWeek.SATURDAY)
            .with(LocalTime.of(0, 1))
        
        return lastSaturday
    }
    
    /**
     * Проверить, нужно ли обновление
     * @return true если последнее обновление было раньше последней субботы 00:01
     */
    fun shouldUpdate(): Boolean {
        val lastUpdate = getLastUpdate()
        val lastSaturday = getLastSaturdayAt001()
        
        return if (lastUpdate == null) {
            Log.d(TAG, "Обновление никогда не выполнялось, требуется обновление")
            true
        } else {
            val needsUpdate = lastUpdate.isBefore(lastSaturday)
            if (needsUpdate) {
                Log.d(TAG, "Требуется обновление: последнее обновление $lastUpdate, последняя суббота $lastSaturday")
            } else {
                Log.d(TAG, "Обновление не требуется: последнее обновление $lastUpdate, последняя суббота $lastSaturday")
            }
            needsUpdate
        }
    }
    
    /**
     * Получить статус обновления
     */
    fun getUpdateStatus(): UpdateStatus {
        val lastUpdate = getLastUpdate()
        val lastSaturday = getLastSaturdayAt001()
        
        return when {
            lastUpdate == null -> UpdateStatus.NEVER_UPDATED
            lastUpdate.isBefore(lastSaturday) -> UpdateStatus.OUTDATED
            else -> UpdateStatus.UP_TO_DATE
        }
    }
    
    /**
     * Получить время следующего обновления (следующая суббота 00:01)
     */
    fun getNextUpdateTime(): LocalDateTime {
        val now = LocalDateTime.now()
        val currentDayOfWeek = now.dayOfWeek
        
        val daysToAdd = when (currentDayOfWeek) {
            DayOfWeek.SATURDAY -> {
                // Если сейчас суббота, проверяем время
                if (now.toLocalTime().isAfter(LocalTime.of(0, 1))) {
                    7 // Следующая суббота
                } else {
                    0 // Сегодняшняя суббота (еще не наступила 00:01)
                }
            }
            else -> {
                val daysUntilSaturday = DayOfWeek.SATURDAY.value - currentDayOfWeek.value
                if (daysUntilSaturday > 0) daysUntilSaturday else 7 + daysUntilSaturday
            }
        }
        
        return now.plusDays(daysToAdd.toLong())
            .with(DayOfWeek.SATURDAY)
            .with(LocalTime.of(0, 1))
    }
}

/**
 * Статус обновления расписания
 */
enum class UpdateStatus {
    NEVER_UPDATED,  // Никогда не обновлялось
    OUTDATED,       // Устарело (требуется обновление)
    UP_TO_DATE      // Актуально
}

