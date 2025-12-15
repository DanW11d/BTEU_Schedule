package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель факультета из API
 */
data class FacultyDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,  // API возвращает "name", а не "name_ru"
    @SerializedName("name_ru")
    val nameRu: String? = null,  // Оставляем для обратной совместимости
    @SerializedName("name_en")
    val nameEn: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true
)

