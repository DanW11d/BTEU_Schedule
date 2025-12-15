package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель группы из API
 */
data class GroupDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("facultyId")
    val facultyId: Int,
    @SerializedName("facultyCode")
    val facultyCode: String? = null,
    @SerializedName("facultyName")
    val facultyName: String? = null,
    @SerializedName("departmentId")
    val departmentId: Int? = null,
    @SerializedName("departmentName")
    val departmentName: String? = null,
    @SerializedName("specialization")
    val specialization: String? = null,
    @SerializedName("course")
    val course: Int,
    @SerializedName("educationForm")
    val educationForm: String,
    @SerializedName("studentCount")
    val studentCount: Int = 0,
    @SerializedName("isActive")
    val isActive: Boolean = true
)

