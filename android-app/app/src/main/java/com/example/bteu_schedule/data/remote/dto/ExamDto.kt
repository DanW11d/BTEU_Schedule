package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель экзамена или зачета из API
 */
data class ExamDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("teacher")
    val teacher: String? = null,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("classroom")
    val classroom: String? = null,
    @SerializedName("examType")
    val examType: String? = null,
    @SerializedName("notes")
    val notes: String? = null
)

