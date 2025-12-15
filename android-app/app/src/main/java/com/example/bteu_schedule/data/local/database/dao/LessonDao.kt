package com.example.bteu_schedule.data.local.database.dao

import androidx.room.*
import com.example.bteu_schedule.data.local.database.entity.CachedLesson
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    
    /**
     * Получить занятия на день
     * ВАЖНО: Ищет занятия с указанным weekParity ИЛИ с weekParity = "both"
     * Это позволяет найти занятия, которые идут на обеих неделях, даже если запрашивается конкретная неделя
     */
    @Query("SELECT * FROM cached_lessons WHERE groupCode = :groupCode AND dayOfWeek = :dayOfWeek AND (weekParity = :weekParity OR weekParity = 'both') ORDER BY pairNumber")
    suspend fun getLessonsByDay(
        groupCode: String,
        dayOfWeek: Int,
        weekParity: String
    ): List<CachedLesson>
    
    @Query("SELECT * FROM cached_lessons WHERE groupCode = :groupCode ORDER BY dayOfWeek, pairNumber")
    suspend fun getLessonsByGroup(groupCode: String): List<CachedLesson>
    
    /**
     * Получить занятия на неделю
     * ВАЖНО: Ищет занятия с указанным weekParity ИЛИ с weekParity = "both"
     * Это позволяет найти занятия, которые идут на обеих неделях, даже если запрашивается конкретная неделя
     */
    @Query("SELECT * FROM cached_lessons WHERE groupCode = :groupCode AND (weekParity = :weekParity OR weekParity = 'both') ORDER BY dayOfWeek, pairNumber")
    suspend fun getLessonsByGroupAndWeek(
        groupCode: String,
        weekParity: String
    ): List<CachedLesson>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CachedLesson)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CachedLesson>)
    
    @Query("DELETE FROM cached_lessons WHERE groupCode = :groupCode")
    suspend fun deleteLessonsByGroup(groupCode: String)
    
    @Query("DELETE FROM cached_lessons WHERE groupCode = :groupCode AND dayOfWeek = :dayOfWeek AND weekParity = :weekParity")
    suspend fun deleteLessonsByDay(
        groupCode: String,
        dayOfWeek: Int,
        weekParity: String
    )
    
    @Query("DELETE FROM cached_lessons WHERE groupCode = :groupCode AND weekParity = :weekParity")
    suspend fun deleteLessonsByGroupAndWeek(
        groupCode: String,
        weekParity: String
    )
    
    @Query("DELETE FROM cached_lessons")
    suspend fun deleteAllLessons()
    
    // Удаление устаревших данных (старше указанного времени)
    @Query("DELETE FROM cached_lessons WHERE cachedAt < :timestamp")
    suspend fun deleteOldLessons(timestamp: Long)
}

