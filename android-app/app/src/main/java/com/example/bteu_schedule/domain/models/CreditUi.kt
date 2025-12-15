package com.example.bteu_schedule.domain.models

/**
 * UI модель зачета
 * Отдельная модель для зачетов (может отличаться от экзаменов)
 */
data class CreditUi(
    val id: Int,
    val groupCode: String,
    val subject: String,
    val teacher: String? = null,
    val date: String,               // "2025-01-15"
    val time: String,               // "10:00"
    val classroom: String? = null,
    val notes: String? = null,
    val isDifferentiated: Boolean = false  // Дифференцированный зачет
)

