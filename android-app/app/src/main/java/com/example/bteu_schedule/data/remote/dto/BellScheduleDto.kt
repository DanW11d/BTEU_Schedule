package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель расписания звонков из API
 */
data class BellScheduleDto(
    @SerializedName("lessonNumber")
    val lessonNumber: Int,
    @SerializedName("lessonStart")
    val lessonStart: String?,
    @SerializedName("lessonEnd")
    val lessonEnd: String?,
    @SerializedName("breakTimeMinutes")
    val breakTimeMinutes: Int = 5,
    @SerializedName("breakAfterLessonMinutes")
    val breakAfterLessonMinutes: Int = 10,
    @SerializedName("description")
    val description: String? = null
)

