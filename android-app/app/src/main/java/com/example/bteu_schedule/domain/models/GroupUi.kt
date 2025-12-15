package com.example.bteu_schedule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI модель группы студентов
 */
@Parcelize
data class GroupUi(
    val code: String,           // "П-1", "И-2", "Z-4"
    val name: String,           // "Группа П-1"
    val course: Int,            // 1, 2, 3, 4
    val specialization: String, // "Экономическое право"
    val facultyCode: String? = null,  // "FEU", "FKIF", etc.
    val facultyName: String? = null,   // "Факультет коммерции и финансов"
    val educationForm: String? = null, // "full_time", "part_time"
    val departmentId: Int? = null,     // ID кафедры (определяется автоматически из группы)
    val departmentName: String? = null // "Кафедра бухгалтерского учета и финансов"
) : Parcelable