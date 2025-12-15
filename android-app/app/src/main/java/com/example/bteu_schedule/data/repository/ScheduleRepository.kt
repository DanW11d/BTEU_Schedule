package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.data.mapper.*
import com.example.bteu_schedule.data.website.WebsiteScheduleRepository
import com.example.bteu_schedule.utils.AppLogger
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.domain.models.*
import com.example.bteu_schedule.utils.ErrorFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с расписанием
 */
@Singleton
class ScheduleRepository @Inject constructor(
    private val api: ScheduleApiService,
    private val websiteRepository: WebsiteScheduleRepository? = null
) {
    
    /**
     * Получить список факультетов
     */
    suspend fun getFaculties(): ApiResponse<List<FacultyUi>> = withContext(Dispatchers.IO) {
        try {
            AppLogger.d("ScheduleRepository", "═══════════════════════════════════════")
            AppLogger.d("ScheduleRepository", "НАЧАЛО ЗАПРОСА ФАКУЛЬТЕТОВ")
            AppLogger.d("ScheduleRepository", "Запрос списка факультетов...")
            val startTime = System.currentTimeMillis()
            val response = api.getFaculties()
            val endTime = System.currentTimeMillis()
            AppLogger.d("ScheduleRepository", "Ответ получен: код=${response.code()}, успех=${response.isSuccessful}, время=${endTime - startTime}ms")
            try {
                AppLogger.d("ScheduleRepository", "URL запроса: ${response.raw().request.url}")
            } catch (e: Exception) {
                AppLogger.d("ScheduleRepository", "Не удалось получить URL запроса: ${e.message}")
            }
            AppLogger.d("ScheduleRepository", "═══════════════════════════════════════")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val faculties = body.map { it.toDomain() }
                    AppLogger.d("ScheduleRepository", "Факультетов получено: ${faculties.size}")
                    ApiResponse.Success(faculties)
                } else {
                    AppLogger.e("ScheduleRepository", "Тело ответа пустое")
                    ApiResponse.Error("Пустой ответ от сервера")
                }
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка HTTP: ${response.code()} - $errorMsg")
                AppLogger.e("ScheduleRepository", "Тело ответа: ${response.errorBody()?.string()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "═══════════════════════════════════════")
            AppLogger.e("ScheduleRepository", "ОШИБКА ПОДКЛЮЧЕНИЯ К СЕРВЕРУ")
            AppLogger.e("ScheduleRepository", "Тип исключения: ${e.javaClass.name}")
            AppLogger.e("ScheduleRepository", "Сообщение: ${e.message}")
            AppLogger.e("ScheduleRepository", "Причина: ${e.cause?.message}")
            AppLogger.e("ScheduleRepository", "Stack trace:", e)
            e.printStackTrace()
            AppLogger.e("ScheduleRepository", "═══════════════════════════════════════")
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Получить группы по факультету, форме обучения и курсу
     */
    suspend fun getGroups(facultyCode: String?, educationForm: String?, course: Int?): ApiResponse<List<GroupUi>> = withContext(Dispatchers.IO) {
        try {
            if (facultyCode == null || educationForm == null) return@withContext ApiResponse.Success(emptyList())
            val response = api.getGroups(facultyCode, educationForm)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    var groupDtos = body
                    
                    if (course != null) {
                        groupDtos = groupDtos.filter { it.course == course }
                    }
                    
                    val groups = groupDtos.map { it.toDomain() }
                    ApiResponse.Success(groups)
                } else {
                    ApiResponse.Error("Пустой ответ от сервера")
                }
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Получить расписание на день
     */
    suspend fun getDaySchedule(
        groupCode: String,
        dayOfWeek: Int,
        weekParity: String? = null
    ): ApiResponse<List<LessonUi>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDaySchedule(groupCode, dayOfWeek, weekParity)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val lessons = body.map { it.toDomain(null) }
                    ApiResponse.Success(lessons)
                } else {
                    ApiResponse.Error("Пустой ответ от сервера")
                }
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    // Other methods remain unchanged
     suspend fun getDepartments(facultyId: Int, facultyCode: String): ApiResponse<List<DepartmentUi>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDepartments(facultyId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val departments = body.map { it.toDomain(facultyCode) }
                ApiResponse.Success(departments)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка загрузки кафедр: ${response.code()} ${response.message()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "Ошибка сети при загрузке кафедр", e)
            ApiResponse.Error(errorMsg)
        }
    }

    suspend fun getWeekSchedule(
        groupCode: String,
        weekParity: String? = null
    ): ApiResponse<List<LessonUi>> = withContext(Dispatchers.IO) {
        try {
            val bellScheduleResponse = try { api.getBellSchedule() } catch(_: Exception) { null }
            val bellScheduleMap = if (bellScheduleResponse?.isSuccessful == true && bellScheduleResponse.body() != null) {
                bellScheduleResponse.body()!!.associateBy { it.lessonNumber }
                    .mapValues { it.value.toDomain() }
            } else {
                null
            }
            
            val response = api.getWeekSchedule(groupCode, weekParity)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val lessons = body.map { it.toDomain(bellScheduleMap) }
                ApiResponse.Success(lessons)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка загрузки расписания на неделю: ${response.code()} ${response.message()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "Ошибка сети при загрузке расписания на неделю", e)
            ApiResponse.Error(errorMsg)
        }
    }

    /**
     * Получить все кафедры
     */
    suspend fun getAllDepartments(): ApiResponse<List<DepartmentUi>> = withContext(Dispatchers.IO) {
        try {
            // Сначала пробуем получить с сайта
            if (websiteRepository != null) {
                try {
                    val websiteDepartments = websiteRepository.getDepartments()
                    if (websiteDepartments.isNotEmpty()) {
                        AppLogger.d("ScheduleRepository", "Кафедры загружены с сайта: ${websiteDepartments.size}")
                        return@withContext ApiResponse.Success(websiteDepartments)
                    }
                } catch (e: Exception) {
                    AppLogger.w("ScheduleRepository", "Ошибка загрузки кафедр с сайта: ${e.message}")
                }
            }
            
            // Fallback на API
            val response = api.getAllDepartments()
            if (response.isSuccessful && response.body() != null) {
                val departments = response.body()!!.map { it.toDomain() }
                AppLogger.d("ScheduleRepository", "Кафедры загружены через API: ${departments.size}")
                ApiResponse.Success(departments)
            } else {
                // Если API тоже не работает, возвращаем статические данные
                AppLogger.w("ScheduleRepository", "API не вернул кафедры, используем статические данные")
                val staticDepartments = com.example.bteu_schedule.data.website.WebsiteScraper.getStaticDepartments()
                ApiResponse.Success(staticDepartments)
            }
        } catch (e: Exception) {
            // При любой ошибке возвращаем статические данные
            AppLogger.w("ScheduleRepository", "Ошибка загрузки кафедр: ${e.message}, используем статические данные")
            val staticDepartments = com.example.bteu_schedule.data.website.WebsiteScraper.getStaticDepartments()
            ApiResponse.Success(staticDepartments)
        }
    }
    
    suspend fun getBellSchedule(): ApiResponse<List<BellScheduleUi>> = withContext(Dispatchers.IO) {
        try {
            // Сначала пробуем получить с сайта
            if (websiteRepository != null) {
                try {
                    val websiteSchedule = websiteRepository.getBellSchedule()
                    if (websiteSchedule.isNotEmpty()) {
                        AppLogger.d("ScheduleRepository", "Расписание звонков загружено с сайта: ${websiteSchedule.size} пар")
                        return@withContext ApiResponse.Success(websiteSchedule)
                    }
                } catch (e: Exception) {
                    AppLogger.w("ScheduleRepository", "Ошибка загрузки расписания звонков с сайта: ${e.message}")
                }
            }
            
            // Fallback на API
            val response = api.getBellSchedule()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val schedule = body.map { it.toDomain() }
                AppLogger.d("ScheduleRepository", "Расписание звонков загружено через API: ${schedule.size} пар")
                ApiResponse.Success(schedule)
            } else {
                // Если API тоже не работает, возвращаем статическое расписание
                AppLogger.w("ScheduleRepository", "API не вернул расписание звонков, используем статическое")
                val staticSchedule = com.example.bteu_schedule.data.website.WebsiteScraper.getStaticBellSchedule()
                ApiResponse.Success(staticSchedule)
            }
        } catch (e: Exception) {
            // При любой ошибке возвращаем статическое расписание
            AppLogger.w("ScheduleRepository", "Ошибка загрузки расписания звонков: ${e.message}, используем статическое")
            val staticSchedule = com.example.bteu_schedule.data.website.WebsiteScraper.getStaticBellSchedule()
            ApiResponse.Success(staticSchedule)
        }
    }

    suspend fun searchLessons(query: String, groupCode: String? = null): ApiResponse<List<LessonUi>> = withContext(Dispatchers.IO) {
        try {
            val bellScheduleResponse = try { api.getBellSchedule() } catch(_: Exception) { null }
            val bellScheduleMap = if (bellScheduleResponse?.isSuccessful == true && bellScheduleResponse.body() != null) {
                bellScheduleResponse.body()!!.associateBy { it.lessonNumber }
                    .mapValues { it.value.toDomain() }
            } else {
                null
            }
            
            val response = api.searchLessons(query, groupCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val lessons = body.map { it.toDomain(bellScheduleMap) }
                ApiResponse.Success(lessons)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка поиска: ${response.code()} ${response.message()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "Ошибка сети при поиске", e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Получить расписание экзаменов для группы
     */
    suspend fun getExams(groupCode: String): ApiResponse<List<ExamUi>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getExams(groupCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val exams = body.map { it.toDomain() }
                ApiResponse.Success(exams)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка загрузки экзаменов: ${response.code()} ${response.message()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "Ошибка сети при загрузке экзаменов", e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Получить расписание зачетов для группы
     */
    suspend fun getTests(groupCode: String): ApiResponse<List<ExamUi>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTests(groupCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body() ?: return@withContext ApiResponse.Error("Пустой ответ от сервера")
                val tests = body.map { it.toDomain() }
                ApiResponse.Success(tests)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e("ScheduleRepository", "Ошибка загрузки зачетов: ${response.code()} ${response.message()}")
                ApiResponse.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e("ScheduleRepository", "Ошибка сети при загрузке зачетов", e)
            ApiResponse.Error(errorMsg)
        }
    }
}