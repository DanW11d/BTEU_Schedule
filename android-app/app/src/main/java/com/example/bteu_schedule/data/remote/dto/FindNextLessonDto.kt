package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FindNextLessonRequest(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("group_code")
    val groupCode: String
)

data class FindNextLessonResponse(
    @SerializedName("found")
    val found: Boolean,
    @SerializedName("lesson")
    val lesson: NextLessonDto? = null,
    @SerializedName("message")
    val message: String? = null
)

data class NextLessonDto(
    @SerializedName("day_of_week")
    val dayOfWeek: Int,
    @SerializedName("lesson_number")
    val lessonNumber: Int,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("teacher")
    val teacher: String?,
    @SerializedName("classroom")
    val classroom: String?,
    @SerializedName("lesson_type")
    val lessonType: String?,
    @SerializedName("days_until")
    val daysUntil: Int?,
    @SerializedName("day_name")
    val dayName: String?
)

