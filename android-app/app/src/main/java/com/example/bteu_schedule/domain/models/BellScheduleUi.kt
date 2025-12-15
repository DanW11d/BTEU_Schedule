package com.example.bteu_schedule.domain.models

/**
 * UI модель расписания звонков (пары)
 */
data class BellScheduleUi(
    val lessonNumber: Int,         // 1-7
    val lessonStart: String?,       // "09:00" (может быть null)
    val lessonEnd: String?,         // "10:35" (может быть null)
    val breakTimeMinutes: Int = 5,
    val breakAfterLessonMinutes: Int = 10,
    val description: String? = null
)

