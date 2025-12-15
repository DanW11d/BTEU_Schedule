package com.example.bteu_schedule.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity для кеширования факультетов в Room Database
 */
@Entity(
    tableName = "cached_faculties",
    indices = [Index(value = ["code"], unique = true)]
)
data class CachedFaculty(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val code: String,
    val name: String,
    val description: String,
    val cachedAt: Long = System.currentTimeMillis() // Время кеширования
)

