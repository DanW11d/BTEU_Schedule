package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель занятия из API
 */
data class LessonDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("group_id")
    val groupId: Int,
    @SerializedName("day_of_week")
    val dayOfWeek: Int,
    @SerializedName("lesson_number")
    val lessonNumber: Int,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("teacher")
    val teacher: String? = null,
    @SerializedName("classroom")
    val classroom: String? = null,
    @SerializedName("lesson_type")
    val lessonType: String? = null,
    @SerializedName("week_parity")
    val weekParity: String? = null,
    @SerializedName("building")
    val building: String? = null,
    @SerializedName("time_start")
    val timeStart: String? = null,
    @SerializedName("time_end")
    val timeEnd: String? = null
)

