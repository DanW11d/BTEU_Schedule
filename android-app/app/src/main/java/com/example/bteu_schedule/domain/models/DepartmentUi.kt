package com.example.bteu_schedule.domain.models

/**
 * UI модель кафедры
 */
data class DepartmentUi(
    val id: Int,
    val code: String,              // "PRET", "IVCS", "BACC"
    val name: String,              // "Право и экономические теории"
    val facultyCode: String,        // "FEU", "FKIF"
    val description: String? = null
)

