package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель кафедры из API
 */
data class DepartmentDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val nameRu: String,  // Бэкенд возвращает "name" (это name_ru из БД)
    @SerializedName("name_en")
    val nameEn: String? = null,
    @SerializedName("faculty_id")
    val facultyId: Int,
    @SerializedName("faculty_code")
    val facultyCode: String? = null,  // Код факультета (для endpoint /departments)
    @SerializedName("faculty_name")
    val facultyName: String? = null,  // Название факультета (для endpoint /departments)
    @SerializedName("description")
    val description: String? = null
)

