package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/**
 * Интерфейс репозитория для работы с расписанием
 * Предоставляет полный API для получения данных
 */
interface IScheduleRepository {
    
    /**
     * Инициализация репозитория
     * Загружает данные при первом запуске
     */
    suspend fun init()
    
    /**
     * Обновить данные, если требуется
     * Проверяет необходимость обновления и выполняет его
     */
    suspend fun refreshIfNeeded()
    
    /**
     * Принудительное обновление данных
     */
    suspend fun forceRefresh()
    
    /**
     * Получить список факультетов
     */
    suspend fun getFaculties(): List<FacultyUi>
    
    /**
     * Получить группы по факультету
     * @param facultyCode Код факультета (опционально)
     * @param educationForm Форма обучения (опционально)
     * @param course Курс (опционально)
     */
    suspend fun getGroups(
        facultyCode: String? = null,
        educationForm: String? = null,
        course: Int? = null
    ): List<GroupUi>
    
    /**
     * Получить расписание занятий для группы
     * @param groupCode Код группы
     * @param dayOfWeek День недели (1-6, опционально)
     * @param weekParity Четность недели ("odd"/"even", опционально)
     */
    suspend fun getLessons(
        groupCode: String,
        dayOfWeek: Int? = null,
        weekParity: String? = null
    ): List<LessonUi>
    
    /**
     * Получить экзамены для группы
     * @param groupCode Код группы
     */
    suspend fun getExams(groupCode: String): List<ExamUi>
    
    /**
     * Получить зачеты для группы
     * @param groupCode Код группы
     */
    suspend fun getCredits(groupCode: String): List<CreditUi>
    
    /**
     * Получить список преподавателей
     * @param departmentCode Код кафедры (опционально)
     */
    suspend fun getTeachers(departmentCode: String? = null): List<TeacherUi>
    
    /**
     * Получить список аудиторий
     * @param building Номер корпуса (опционально)
     */
    suspend fun getAuditoriums(building: String? = null): List<AuditoriumUi>
    
    /**
     * Получить информацию об аудитории
     * @param code Код аудитории (например, "3-11")
     */
    suspend fun getAuditoriumInfo(code: String): AuditoriumUi?
    
    /**
     * Получить время последнего обновления
     */
    val lastUpdate: LocalDateTime?
    
    /**
     * Получить статус обновления
     */
    val status: StateFlow<ScheduleStatus>
}

/**
 * Статус расписания
 */
sealed class ScheduleStatus {
    object Outdated : ScheduleStatus()      // Данные устарели, требуется обновление
    object Updating : ScheduleStatus()      // Идет обновление
    object Updated : ScheduleStatus()       // Данные актуальны
    data class Error(val message: String) : ScheduleStatus()  // Ошибка обновления
}

