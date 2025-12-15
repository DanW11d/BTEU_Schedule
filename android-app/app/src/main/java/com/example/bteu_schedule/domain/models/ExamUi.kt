package com.example.bteu_schedule.domain.models

/**
 * UI модель экзамена или зачета
 */
data class ExamUi(
    val id: Int,
    val groupCode: String,
    val subject: String,
    val teacher: String? = null,
    val date: String,               // "2025-01-15"
    val time: String,               // "10:00"
    val classroom: String? = null,
    val examType: String? = null,  // "exam", "test"
    val notes: String? = null
)

