package com.example.bteu_schedule.data.remote.api

import com.example.bteu_schedule.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API интерфейс для работы с расписанием
 */
interface ScheduleApiService {
    
    /**
     * Получить список факультетов
     */
    @GET("faculties")
    suspend fun getFaculties(): Response<List<FacultyDto>>
    
    /**
     * Получить кафедры факультета
     */
    @GET("faculties/{facultyId}/departments")
    suspend fun getDepartments(@Path("facultyId") facultyId: Int): Response<List<DepartmentDto>>
    
    /**
     * Получить все кафедры
     */
    @GET("departments")
    suspend fun getAllDepartments(): Response<List<DepartmentDto>>
    
    /**
     * Получить группы по факультету и форме обучения
     */
    @GET("groups")
    suspend fun getGroups(
        @Query("faculty") facultyCode: String? = null,
        @Query("form") educationForm: String? = null
    ): Response<List<GroupDto>>
    
    /**
     * Получить информацию о группе
     */
    @GET("groups/{code}")
    suspend fun getGroup(@Path("code") groupCode: String): Response<GroupDto>
    
    /**
     * Получить расписание на день
     */
    @GET("schedule/group/{code}/day/{day}")
    suspend fun getDaySchedule(
        @Path("code") groupCode: String,
        @Path("day") dayOfWeek: Int,
        @Query("week") weekParity: String? = null
    ): Response<List<LessonDto>>
    
    /**
     * Получить расписание на неделю
     */
    @GET("schedule/group/{code}/week")
    suspend fun getWeekSchedule(
        @Path("code") groupCode: String,
        @Query("week") weekParity: String? = null
    ): Response<List<LessonDto>>
    
    /**
     * Получить текущее расписание
     */
    @GET("schedule/group/{code}/current")
    suspend fun getCurrentSchedule(
        @Path("code") groupCode: String
    ): Response<List<LessonDto>>
    
    /**
     * Получить расписание звонков
     */
    @GET("bell-schedule")
    suspend fun getBellSchedule(): Response<List<BellScheduleDto>>
    
    /**
     * Поиск по расписанию
     */
    @GET("search")
    suspend fun searchLessons(
        @Query("q") query: String,
        @Query("group") groupCode: String? = null
    ): Response<List<LessonDto>>
    
    /**
     * Получить уведомления пользователя
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("unread_only") unreadOnly: Boolean = false
    ): Response<List<NotificationDto>>
    
    /**
     * Отметить уведомление как прочитанное
     */
    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") notificationId: Int): Response<Unit>
    
    /**
     * Получить количество непрочитанных уведомлений
     */
    @GET("notifications/unread/count")
    suspend fun getUnreadNotificationsCount(): Response<Int>
    
    /**
     * Получить расписание экзаменов для группы
     */
    @GET("exams/group/{code}")
    suspend fun getExams(@Path("code") groupCode: String): Response<List<ExamDto>>
    
    /**
     * Получить расписание зачетов для группы
     */
    @GET("tests/group/{code}")
    suspend fun getTests(@Path("code") groupCode: String): Response<List<ExamDto>>
    
    /**
     * Отправить сообщение в AI чат
     */
    @POST("ai/chat")
    suspend fun sendAiMessage(@Body request: com.example.bteu_schedule.data.remote.dto.AiChatRequest): Response<com.example.bteu_schedule.data.remote.dto.AiChatResponse>
    
    /**
     * Проверить статус AI сервиса
     */
    @GET("ai/status")
    suspend fun getAiStatus(): Response<com.example.bteu_schedule.data.remote.dto.AiStatusResponse>
    
    /**
     * Получить аналитику расписания для группы
     */
    @GET("analytics/group/{code}")
    suspend fun getScheduleAnalytics(@Path("code") groupCode: String): Response<com.example.bteu_schedule.data.remote.dto.ScheduleAnalyticsDto>
    
    /**
     * Найти следующее занятие по предмету
     */
    @POST("ai/find-next-lesson")
    suspend fun findNextLesson(@Body request: com.example.bteu_schedule.data.remote.dto.FindNextLessonRequest): Response<com.example.bteu_schedule.data.remote.dto.FindNextLessonResponse>
}

