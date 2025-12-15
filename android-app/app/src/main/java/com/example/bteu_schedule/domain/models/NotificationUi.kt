package com.example.bteu_schedule.domain.models

/**
 * UI модель уведомления
 */
data class NotificationUi(
    val id: Int,
    val title: String,
    val message: String,
    val type: String, // "schedule_update", "exam", "announcement", "reminder"
    val isRead: Boolean,
    val createdAt: String // Дата в формате строки
)

