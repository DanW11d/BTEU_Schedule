package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.data.mapper.toDomain
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.domain.models.NotificationUi
import com.example.bteu_schedule.data.repository.utils.toApiResponse
import com.example.bteu_schedule.data.repository.utils.toApiResponseList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с уведомлениями
 * 
 * Оптимизирован: использует extension функции для устранения дублирования кода
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val api: ScheduleApiService
) {
    
    /**
     * Получить уведомления пользователя
     */
    suspend fun getNotifications(unreadOnly: Boolean = false): ApiResponse<List<NotificationUi>> {
        return api.getNotifications(unreadOnly)
            .toApiResponseList(
                tag = "NotificationRepository",
                operationName = "Получение уведомлений",
                transform = { it.toDomain() }
            )
    }
    
    /**
     * Отметить уведомление как прочитанное
     */
    suspend fun markAsRead(notificationId: Int): ApiResponse<Unit> {
        return api.markNotificationAsRead(notificationId)
            .toApiResponse(
                tag = "NotificationRepository",
                operationName = "Отметка уведомления как прочитанного"
            )
    }
    
    /**
     * Получить количество непрочитанных уведомлений
     */
    suspend fun getUnreadCount(): ApiResponse<Int> {
        return api.getUnreadNotificationsCount()
            .toApiResponse(
                tag = "NotificationRepository",
                operationName = "Получение количества непрочитанных уведомлений",
                transform = { it }
            )
    }
}