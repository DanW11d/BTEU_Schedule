package com.example.bteu_schedule.domain.models

/**
 * UI модель преподавателя
 */
data class TeacherUi(
    val id: Int,
    val fullName: String,          // "Тропкова Е.Г."
    val email: String? = null,
    val phone: String? = null,
    val departmentCode: String? = null,
    val academicTitle: String? = null,  // "доцент", "профессор"
    val position: String? = null,
    val officeNumber: String? = null,
    val officeBuilding: String? = null
)

