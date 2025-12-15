package com.example.bteu_schedule.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity для кеширования экзаменов и зачетов в Room Database
 */
@Entity(
    tableName = "cached_exams",
    indices = [
        Index(value = ["groupCode", "date"]),
        Index(value = ["groupCode"])
    ]
)
data class CachedExam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Int, // ID из API
    val groupCode: String,
    val subject: String,
    val teacher: String?,
    val date: String,
    val time: String,
    val classroom: String?,
    val examType: String?, // "exam", "test"
    val notes: String?,
    val cachedAt: Long = System.currentTimeMillis() // Время кеширования
)

