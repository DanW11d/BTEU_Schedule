package com.example.bteu_schedule.data.local.database.dao

import androidx.room.*
import com.example.bteu_schedule.data.local.database.entity.CachedExam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    
    @Query("SELECT * FROM cached_exams WHERE groupCode = :groupCode ORDER BY date, time")
    suspend fun getExamsByGroup(groupCode: String): List<CachedExam>
    
    @Query("SELECT * FROM cached_exams WHERE groupCode = :groupCode AND examType = :examType ORDER BY date, time")
    suspend fun getExamsByGroupAndType(groupCode: String, examType: String): List<CachedExam>
    
    @Query("SELECT * FROM cached_exams WHERE groupCode = :groupCode AND date = :date ORDER BY time")
    suspend fun getExamsByGroupAndDate(groupCode: String, date: String): List<CachedExam>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: CachedExam)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<CachedExam>)
    
    @Query("DELETE FROM cached_exams WHERE groupCode = :groupCode")
    suspend fun deleteExamsByGroup(groupCode: String)
    
    @Query("DELETE FROM cached_exams WHERE groupCode = :groupCode AND examType = :examType")
    suspend fun deleteExamsByGroupAndType(groupCode: String, examType: String)
    
    @Query("DELETE FROM cached_exams")
    suspend fun deleteAllExams()
    
    // Удаление устаревших данных
    @Query("DELETE FROM cached_exams WHERE cachedAt < :timestamp")
    suspend fun deleteOldExams(timestamp: Long)
}

