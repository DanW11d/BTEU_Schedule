package com.example.bteu_schedule.domain.models

/**
 * UI модель занятия (пары)
 */
data class LessonUi(
    val id: Int,
    val pairNumber: Int,        // 1-7
    val dayOfWeek: Int,         // 1-6 (Понедельник-Суббота)
    val time: String,           // "09:00-10:35"
    val subject: String,        // "Бухгалтерский учет"
    val teacher: String,        // "доц. Тропкова Е.Г."
    val classroom: String,      // "3-11"
    val type: String,           // "lecture" / "practice"
    val weekParity: String      // "odd" / "even" / "both"
)

