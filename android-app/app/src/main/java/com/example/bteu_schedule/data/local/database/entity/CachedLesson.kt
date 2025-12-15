package com.example.bteu_schedule.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity для кеширования расписания занятий в Room Database
 */
@Entity(
    tableName = "cached_lessons",
    indices = [
        Index(value = ["groupCode", "dayOfWeek", "weekParity"]),
        Index(value = ["groupCode"])
    ]
)
data class CachedLesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: Int, // ID из API
    val groupCode: String,
    val pairNumber: Int,
    val dayOfWeek: Int,
    val time: String,
    val subject: String,
    val teacher: String,
    val classroom: String,
    val type: String,
    val weekParity: String,
    val cachedAt: Long = System.currentTimeMillis() // Время кеширования
)

