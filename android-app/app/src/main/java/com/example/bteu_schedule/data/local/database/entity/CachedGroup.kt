package com.example.bteu_schedule.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity для кеширования групп в Room Database
 * Использует составной первичный ключ (code + facultyCode) для предотвращения перезаписи
 * групп с одинаковым кодом, но разными факультетами
 */
@Entity(
    tableName = "cached_groups",
    primaryKeys = ["code", "facultyCode"],
    indices = [
        Index(value = ["code"]),
        Index(value = ["facultyCode"]),
        // Составные индексы для оптимизации часто используемых запросов
        Index(value = ["facultyCode", "educationForm"]),
        Index(value = ["facultyCode", "educationForm", "course"]),
        Index(value = ["course"]) // Для фильтрации по курсу
    ]
)
data class CachedGroup(
    val code: String,
    val name: String,
    val course: Int,
    val specialization: String,
    val facultyCode: String, // Теперь обязательное поле (не nullable)
    val facultyName: String?,
    val educationForm: String?,
    val departmentId: Int?,
    val departmentName: String?,
    val cachedAt: Long = System.currentTimeMillis() // Время кеширования
)

