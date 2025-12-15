package com.example.bteu_schedule.domain.models

/**
 * UI модель факультета
 */
data class FacultyUi(
    val id: Int,                // ID факультета из БД
    val code: String,           // "FEU", "FKIF"
    val name: String,           // "Факультет экономики и управления"
    val description: String     // "ФЭУ"
)

