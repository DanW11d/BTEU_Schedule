package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO модель уведомления из API
 */
data class NotificationDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("notification_type")
    val notificationType: String? = null,
    @SerializedName("related_group_id")
    val relatedGroupId: Int? = null,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String
)

