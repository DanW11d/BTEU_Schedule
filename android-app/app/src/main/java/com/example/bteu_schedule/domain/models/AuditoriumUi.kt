package com.example.bteu_schedule.domain.models

/**
 * UI модель аудитории
 */
data class AuditoriumUi(
    val id: Int,
    val code: String,              // "3-11", "1-205"
    val name: String,              // Полное название
    val building: String? = null,   // Номер корпуса
    val floor: Int? = null,        // Этаж
    val capacity: Int? = null,     // Вместимость
    val type: String? = null,      // Тип аудитории (лекционная, лабораторная и т.д.)
    val equipment: String? = null,  // Оборудование
    val description: String? = null
)

