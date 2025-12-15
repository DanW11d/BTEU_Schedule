package com.example.bteu_schedule.data.website

import android.util.Log
import com.example.bteu_schedule.data.config.AppConfig
import com.example.bteu_schedule.domain.models.BellScheduleUi
import com.example.bteu_schedule.domain.models.DepartmentUi
import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с парсером сайта БТЭУ
 */
class WebsiteScheduleRepository {
    
    private val TAG = "WebsiteScheduleRepository"
    
    /**
     * Получить список факультетов
     */
    suspend fun getFaculties(): List<FacultyUi> = withContext(Dispatchers.IO) {
        try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Получение списка факультетов с сайта")
            }
            WebsiteScraper.getFaculties()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении факультетов: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Получить список групп для факультета
     */
    suspend fun getGroups(facultyCode: String): List<GroupUi> = withContext(Dispatchers.IO) {
        try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Получение групп для факультета: $facultyCode")
            }
            WebsiteScraper.getGroups(facultyCode)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении групп: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Получить расписание для группы
     */
    suspend fun getSchedule(
        groupCode: String,
        facultyCode: String,
        dayOfWeek: Int? = null,
        weekParity: String? = null
    ): List<LessonUi> = withContext(Dispatchers.IO) {
        try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Получение расписания для группы: $groupCode, факультет: $facultyCode, день: $dayOfWeek, неделя: $weekParity")
            }
            WebsiteScraper.getSchedule(groupCode, facultyCode, dayOfWeek, weekParity)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении расписания: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Получить расписание звонков с сайта
     */
    suspend fun getBellSchedule(): List<BellScheduleUi> = withContext(Dispatchers.IO) {
        try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Получение расписания звонков с сайта")
            }
            WebsiteScraper.getBellSchedule()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении расписания звонков: ${e.message}", e)
            // Возвращаем статическое расписание при ошибке
            WebsiteScraper.getStaticBellSchedule()
        }
    }
    
    /**
     * Получить список всех кафедр
     */
    suspend fun getDepartments(): List<DepartmentUi> = withContext(Dispatchers.IO) {
        try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Получение списка кафедр")
            }
            WebsiteScraper.getDepartments()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении кафедр: ${e.message}", e)
            emptyList()
        }
    }
}

