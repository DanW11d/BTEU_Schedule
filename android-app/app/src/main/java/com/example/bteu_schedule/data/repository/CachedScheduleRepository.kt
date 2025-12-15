
package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.data.website.WebsiteScheduleRepository
import com.example.bteu_schedule.utils.AppLogger
import com.example.bteu_schedule.data.local.database.dao.ExamDao
import com.example.bteu_schedule.data.local.database.dao.FacultyDao
import com.example.bteu_schedule.data.local.database.dao.GroupDao
import com.example.bteu_schedule.data.local.database.dao.LessonDao
import com.example.bteu_schedule.data.local.database.mapper.toDomain as cachedToDomain
import com.example.bteu_schedule.data.local.database.mapper.toEntity
import com.example.bteu_schedule.data.mapper.toDomain as dtoToDomain
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.domain.models.ExamUi
import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class CachedScheduleRepository(
    private val facultyDao: FacultyDao,
    private val groupDao: GroupDao,
    private val lessonDao: LessonDao,
    private val examDao: ExamDao,
    private val apiService: ScheduleApiService,
    private val websiteRepository: WebsiteScheduleRepository? = null
) {

    /**
     * Запускает полную синхронизацию данных с API сервера
     */
    suspend fun syncAllDataFromServer(): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val apiError = mutableListOf<String>()
        
        try {
            AppLogger.debugSection("CachedScheduleRepository", "НАЧАЛО СИНХРОНИЗАЦИИ ДАННЫХ") {
                AppLogger.d("CachedScheduleRepository", "API - основной источник данных")
            }
            
            // Загружаем данные через парсер сайта (если доступен) или API
            try {
                if (websiteRepository != null) {
                    AppLogger.d("CachedScheduleRepository", "Попытка загрузки данных через парсер сайта...")
                    
                    // Получаем факультеты с сайта
                    val faculties = websiteRepository.getFaculties()
                    if (faculties.isNotEmpty()) {
                        facultyDao.insertFaculties(faculties.map { it.toEntity() })
                        AppLogger.d("CachedScheduleRepository", "Факультеты загружены с сайта: ${faculties.size}")
                        
                        // Загружаем группы для всех факультетов
                        val allGroups = mutableListOf<GroupUi>()
                        for (faculty in faculties) {
                            try {
                                val groups = websiteRepository.getGroups(faculty.code)
                                allGroups.addAll(groups)
                                AppLogger.d("CachedScheduleRepository", "Группы для ${faculty.code}: ${groups.size}")
                            } catch (e: Exception) {
                                AppLogger.w("CachedScheduleRepository", "Ошибка загрузки групп для ${faculty.code}: ${e.message}")
                            }
                        }
                        
                        if (allGroups.isNotEmpty()) {
                            groupDao.insertGroups(allGroups.map { it.toEntity() })
                            AppLogger.d("CachedScheduleRepository", "Группы загружены с сайта: ${allGroups.size}")
                        }
                        
                        // Очищаем кэш занятий и экзаменов
                        try {
                            lessonDao.deleteAllLessons()
                            examDao.deleteAllExams()
                            AppLogger.d("CachedScheduleRepository", "Кэш занятий и экзаменов очищен для обновления")
                        } catch (e: Exception) {
                            AppLogger.w("CachedScheduleRepository", "Ошибка очистки кэша занятий", e)
                        }
                        
                        AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
                        AppLogger.d("CachedScheduleRepository", "УСПЕХ! Данные загружены с сайта")
                        AppLogger.d("CachedScheduleRepository", "Групп: ${allGroups.size}")
                        AppLogger.d("CachedScheduleRepository", "Факультетов: ${faculties.size}")
                        AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
                        return@withContext ApiResponse.Success(Unit)
                    }
                }
                
                // Fallback на API, если парсер сайта недоступен или не вернул данные
                AppLogger.d("CachedScheduleRepository", "Попытка загрузки данных через API...")
                val facultiesResponse = apiService.getFaculties()
                
                if (facultiesResponse.isSuccessful) {
                    val facultiesBody = facultiesResponse.body()
                    if (facultiesBody != null) {
                        val faculties = facultiesBody.map { it.dtoToDomain() }
                        if (faculties.isNotEmpty()) {
                            facultyDao.insertFaculties(faculties.map { it.toEntity() })
                            AppLogger.d("CachedScheduleRepository", "Факультеты загружены через API: ${faculties.size}")
                            
                            // Загружаем группы для всех факультетов
                            val allGroups = mutableListOf<GroupUi>()
                            for (faculty in faculties) {
                                try {
                                    val groupsResponse = apiService.getGroups(faculty.code, null)
                                    if (groupsResponse.isSuccessful) {
                                        val groupsBody = groupsResponse.body()
                                        if (groupsBody != null) {
                                            val groups = groupsBody.map { it.dtoToDomain() }
                                            allGroups.addAll(groups)
                                        } else {
                                            AppLogger.w("CachedScheduleRepository", "Пустой ответ при загрузке групп для ${faculty.code}")
                                        }
                                    } else {
                                        AppLogger.w("CachedScheduleRepository", "Ошибка загрузки групп для ${faculty.code}: HTTP ${groupsResponse.code()}")
                                    }
                                } catch (e: Exception) {
                                    AppLogger.w("CachedScheduleRepository", "Ошибка загрузки групп для ${faculty.code}: ${e.message}")
                                }
                            }
                            
                            if (allGroups.isNotEmpty()) {
                                groupDao.insertGroups(allGroups.map { it.toEntity() })
                                AppLogger.d("CachedScheduleRepository", "Группы загружены через API: ${allGroups.size}")
                            }
                            
                            // Очищаем кэш занятий и экзаменов
                            try {
                                lessonDao.deleteAllLessons()
                                examDao.deleteAllExams()
                                AppLogger.d("CachedScheduleRepository", "Кэш занятий и экзаменов очищен для обновления")
                            } catch (e: Exception) {
                                AppLogger.w("CachedScheduleRepository", "Ошибка очистки кэша занятий", e)
                            }
                            
                            AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
                            AppLogger.d("CachedScheduleRepository", "УСПЕХ! Данные загружены через API")
                            AppLogger.d("CachedScheduleRepository", "Групп: ${allGroups.size}")
                            AppLogger.d("CachedScheduleRepository", "Факультетов: ${faculties.size}")
                            AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
                            return@withContext ApiResponse.Success(Unit)
                        } else {
                            apiError.add("API вернул пустой список факультетов")
                            AppLogger.w("CachedScheduleRepository", "API вернул пустой список факультетов")
                        }
                    } else {
                        apiError.add("API вернул пустое тело ответа")
                        AppLogger.w("CachedScheduleRepository", "API вернул пустое тело ответа")
                    }
                } else {
                    val errorMsg = "HTTP ${facultiesResponse.code()}: ${facultiesResponse.message()}"
                    apiError.add(errorMsg)
                    AppLogger.e("CachedScheduleRepository", "Ошибка API: $errorMsg")
                    try {
                        val errorBody = facultiesResponse.errorBody()?.string()
                        if (!errorBody.isNullOrBlank()) {
                            AppLogger.e("CachedScheduleRepository", "Тело ошибки: $errorBody")
                        }
                    } catch (e: Exception) {
                        // Игнорируем ошибку чтения тела
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Таймаут подключения к серверу. Сервер не отвечает"
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> "Сервер недоступен. Проверьте интернет-соединение"
                    e.message?.contains("Failed to connect", ignoreCase = true) == true -> "Не удалось подключиться к серверу"
                    e.message?.contains("SSL", ignoreCase = true) == true -> "Ошибка SSL сертификата"
                    e.message?.contains("Certificate", ignoreCase = true) == true -> "Ошибка сертификата сервера"
                    else -> "Сервер недоступен. Проверьте интернет-соединение"
                }
                apiError.add(errorMsg)
                AppLogger.e("CachedScheduleRepository", "═══════════════════════════════════════")
                AppLogger.e("CachedScheduleRepository", "ИСКЛЮЧЕНИЕ ПРИ ЗАГРУЗКЕ ЧЕРЕЗ API")
                AppLogger.e("CachedScheduleRepository", "Тип: ${e.javaClass.simpleName}")
                AppLogger.e("CachedScheduleRepository", "Сообщение: ${e.message}")
                AppLogger.e("CachedScheduleRepository", "Причина: ${e.cause?.message}")
                e.printStackTrace()
                AppLogger.e("CachedScheduleRepository", "═══════════════════════════════════════")
            }
            
            // Формируем итоговое сообщение об ошибке
            val finalErrorMsg = if (apiError.isNotEmpty()) {
                val apiErrorMsg = apiError.first()
                "Не удалось загрузить данные. $apiErrorMsg"
            } else {
                "Не удалось загрузить данные. Проверьте подключение к интернету"
            }
            
            AppLogger.e("CachedScheduleRepository", "═══════════════════════════════════════")
            AppLogger.e("CachedScheduleRepository", "ОШИБКА СИНХРОНИЗАЦИИ")
            AppLogger.e("CachedScheduleRepository", finalErrorMsg)
            AppLogger.e("CachedScheduleRepository", "═══════════════════════════════════════")
            
            ApiResponse.Error(finalErrorMsg)
        } catch (e: Exception) {
            AppLogger.e("CachedScheduleRepository", "Критическая ошибка синхронизации", e)
            ApiResponse.Error("Критическая ошибка синхронизации: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    /**
     * Запускает полную синхронизацию данных с сайта университета.
     */
    suspend fun syncWithWebsite(): Boolean {
        return try {
            if (websiteRepository == null) {
                AppLogger.w("CachedScheduleRepository", "Парсер сайта не доступен")
                return false
            }
            
            // Загружаем факультеты
            val faculties = websiteRepository.getFaculties()
            if (faculties.isNotEmpty()) {
                facultyDao.insertFaculties(faculties.map { it.toEntity() })
            }
            
            // Загружаем группы для всех факультетов
            val allGroups = mutableListOf<GroupUi>()
            for (faculty in faculties) {
                try {
                    val groups = websiteRepository.getGroups(faculty.code)
                    allGroups.addAll(groups)
                } catch (e: Exception) {
                    AppLogger.w("CachedScheduleRepository", "Ошибка загрузки групп для ${faculty.code}: ${e.message}")
                }
            }
            
            if (allGroups.isNotEmpty()) {
                groupDao.insertGroups(allGroups.map { it.toEntity() })
                AppLogger.d("CachedScheduleRepository", "Синхронизация с сайтом успешна: ${allGroups.size} групп, ${faculties.size} факультетов")
                true
            } else {
                AppLogger.w("CachedScheduleRepository", "Сайт вернул пустой список групп")
                false
            }
        } catch (e: Exception) {
            AppLogger.e("CachedScheduleRepository", "Ошибка синхронизации с сайтом", e)
            false
        }
    }

    // --- Методы для получения данных из локальной базы (кэша) --- //

    /**
     * Очистить все данные из базы
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        try {
            AppLogger.d("CachedScheduleRepository", "Очистка всех данных из базы...")
            facultyDao.deleteAllFaculties()
            groupDao.deleteAllGroups()
            lessonDao.deleteAllLessons()
            examDao.deleteAllExams()
            AppLogger.d("CachedScheduleRepository", "База данных очищена (включая занятия и экзамены)")
        } catch (e: Exception) {
            AppLogger.e("CachedScheduleRepository", "Ошибка очистки базы данных", e)
        }
    }

    fun getFaculties(): Flow<List<FacultyUi>> = facultyDao.getAllFaculties()
        .map { faculties -> 
            AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
            AppLogger.d("CachedScheduleRepository", "ЗАГРУЗКА ФАКУЛЬТЕТОВ ИЗ БАЗЫ")
            AppLogger.d("CachedScheduleRepository", "Найдено факультетов: ${faculties.size}")
            faculties.forEach { faculty ->
                AppLogger.d("CachedScheduleRepository", "  - Код: '${faculty.code}', Название: '${faculty.name}'")
            }
            AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
            faculties.map { it.cachedToDomain() } 
        }
        .flowOn(Dispatchers.IO)

    fun getGroups(facultyCode: String, educationForm: String, course: Int): Flow<List<GroupUi>> = flow {
        AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
        AppLogger.d("CachedScheduleRepository", "ЗАПРОС ГРУПП")
        AppLogger.d("CachedScheduleRepository", "facultyCode: '$facultyCode'")
        AppLogger.d("CachedScheduleRepository", "educationForm: '$educationForm'")
        AppLogger.d("CachedScheduleRepository", "course: $course")
        
        // Используем suspend функцию внутри flow через withContext
        val groups = withContext(Dispatchers.IO) {
            // Получаем все группы для факультета (независимо от курса и формы обучения)
            // Затем фильтруем их в коде, чтобы правильно обработать случай course=0
            val allGroupsForFaculty = groupDao.getAllGroupsList()
                .filter { it.facultyCode == facultyCode }
            
            AppLogger.d("CachedScheduleRepository", "Всего групп для факультета '$facultyCode': ${allGroupsForFaculty.size}")
            
            // Фильтруем по форме обучения и курсу
            // Если курс в БД = 0, пробуем извлечь курс из кода группы
            val foundGroups = allGroupsForFaculty
                .filter { group ->
                    // Проверка формы обучения
                    val formMatches = group.educationForm == educationForm || 
                                     educationForm.isBlank() ||
                                     group.educationForm.isNullOrBlank()
                    
                    if (!formMatches) {
                        return@filter false
                    }
                    
                    // Определяем курс группы
                    val groupCourse = if (group.course == 0) {
                        // Пробуем извлечь курс из кода группы
                        val extracted = extractCourseFromCode(group.code)
                        if (extracted > 0) {
                            extracted
                        } else {
                            // Если не удалось извлечь, используем 0 (показываем для всех курсов)
                            // Или можно вернуть null и показывать для всех
                            0
                        }
                    } else {
                        group.course
                    }
                    
                    // Проверка курса
                    val courseMatches = groupCourse == course
                    
                    if (courseMatches && formMatches) {
                        AppLogger.d("CachedScheduleRepository", "✓ Найдена группа: ${group.code}, facultyCode=${group.facultyCode}, course в БД=${group.course}, извлеченный курс=$groupCourse, educationForm=${group.educationForm}")
                    } else {
                        // Логируем только если включено детальное логирование
                        AppLogger.d("CachedScheduleRepository", "✗ Пропущена группа: ${group.code}, facultyCode=${group.facultyCode}, course в БД=${group.course}, извлеченный курс=$groupCourse, educationForm=${group.educationForm} (запрошен курс=$course, форма=$educationForm)")
                    }
                    
                    courseMatches && formMatches
                }
            
            AppLogger.d("CachedScheduleRepository", "Найдено групп после фильтрации: ${foundGroups.size} из ${allGroupsForFaculty.size}")
            
            foundGroups
        }
        AppLogger.d("CachedScheduleRepository", "Возвращаем ${groups.size} групп для facultyCode=$facultyCode, educationForm=$educationForm, course=$course")
        AppLogger.d("CachedScheduleRepository", "═══════════════════════════════════════")
        emit(groups.map { it.cachedToDomain() })
    }.flowOn(Dispatchers.IO)

    fun getDaySchedule(groupCode: String, dayOfWeek: Int, isOddWeek: Boolean, forceRefresh: Boolean = false): Flow<List<LessonUi>> = flow {
        val weekParity = if (isOddWeek) "odd" else "even"
        
        // Если требуется принудительное обновление, очищаем кэш для этой группы и дня
        if (forceRefresh) {
            withContext(Dispatchers.IO) {
                lessonDao.deleteLessonsByDay(groupCode, dayOfWeek, weekParity)
                AppLogger.d("CachedScheduleRepository", "Кэш очищен для принудительного обновления: группа=$groupCode, день=$dayOfWeek, неделя=$weekParity")
            }
        }
        
        // Проверяем, есть ли данные в базе
        val lessonsFromDb = withContext(Dispatchers.IO) {
            lessonDao.getLessonsByDay(groupCode, dayOfWeek, weekParity)
        }
        
        // Если данных нет в базе (или было принудительное обновление) и парсер сайта доступен, загружаем с сайта
        if (lessonsFromDb.isEmpty() && websiteRepository != null) {
            try {
                AppLogger.d("CachedScheduleRepository", "Расписание не найдено в базе или требуется обновление, загружаем с сайта...")
                
                // Получаем факультет группы из базы
                val group = groupDao.getGroupByCode(groupCode)
                val facultyCode = group?.facultyCode ?: "FEU" // По умолчанию FEU
                
                val websiteLessons = websiteRepository.getSchedule(
                    groupCode = groupCode,
                    facultyCode = facultyCode,
                    dayOfWeek = dayOfWeek,
                    weekParity = weekParity
                )
                
                if (websiteLessons.isNotEmpty()) {
                    // Сохраняем в базу для кэширования
                    lessonDao.insertLessons(websiteLessons.map { it.toEntity(groupCode) })
                    AppLogger.d("CachedScheduleRepository", "Загружено ${websiteLessons.size} занятий с сайта и сохранено в базу")
                    emit(websiteLessons)
                } else {
                    AppLogger.d("CachedScheduleRepository", "Сайт не вернул расписание для группы $groupCode")
                    emit(emptyList())
                }
            } catch (e: Exception) {
                AppLogger.e("CachedScheduleRepository", "Ошибка загрузки расписания с сайта", e)
                emit(emptyList())
            }
        } else {
            // Используем данные из базы
            AppLogger.d("CachedScheduleRepository", "Используем данные из кэша: ${lessonsFromDb.size} занятий")
            emit(lessonsFromDb.map { it.cachedToDomain() })
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получить экзамены для группы из кэша
     */
    suspend fun getExams(groupCode: String): ApiResponse<List<ExamUi>> = withContext(Dispatchers.IO) {
        try {
            val exams = examDao.getExamsByGroup(groupCode)
            val examUiList = exams.map { it.cachedToDomain() }
            ApiResponse.Success(examUiList)
        } catch (e: Exception) {
            AppLogger.e("CachedScheduleRepository", "Ошибка получения экзаменов", e)
            ApiResponse.Error("Ошибка получения экзаменов: ${e.message}")
        }
    }

    /**
     * Получить зачеты для группы из кэша
     */
    suspend fun getTests(groupCode: String): ApiResponse<List<ExamUi>> = withContext(Dispatchers.IO) {
        try {
            val exams = examDao.getExamsByGroup(groupCode)
            val examUiList = exams.map { it.cachedToDomain() }
            ApiResponse.Success(examUiList)
        } catch (e: Exception) {
            AppLogger.e("CachedScheduleRepository", "Ошибка получения зачетов", e)
            ApiResponse.Error("Ошибка получения зачетов: ${e.message}")
        }
    }

    /**
     * Извлекает курс из кода группы (временный обходной путь для групп с course=0 в БД).
     * Поддерживает различные форматы:
     * - "2-01" -> 2
     * - "4-12" -> 4
     * - "1-01" -> 1
     * - "Щ-01" -> 1 (из второй части, если первая не число)
     * - "01" -> 0 (код группы без указания курса, курс должен быть в QKURS)
     */
    private fun extractCourseFromCode(code: String?): Int {
        if (code == null || code.isBlank()) return 0
        
        val cleanCode = code.trim()
        
        // Если код группы - это просто число от 1 до 6 (например, "1" -> 1)
        // НО: "01", "02" и т.д. - это код группы, а не курс
        // Однако, если QKURS содержит мусор, для "01" можно попробовать извлечь курс из второй цифры как fallback
        if (cleanCode.length <= 2 && cleanCode.all { it.isDigit() }) {
            val course = cleanCode.toIntOrNull()
            // Если код "01", "02" и т.д. (начинается с 0), это код группы
            // Но если QKURS содержит мусор, вторая цифра может быть курсом (fallback)
            if (cleanCode.length == 2 && cleanCode.startsWith("0")) {
                // Это код группы типа "01", "02"
                // Если вторая цифра от 1 до 6, используем её как курс (fallback когда QKURS мусор)
                val secondDigit = cleanCode[1].toString().toIntOrNull()
                if (secondDigit != null && secondDigit > 0 && secondDigit <= 4) {
                    return secondDigit
                }
            } else if (course != null && course > 0 && course <= 4) {
                // Если код "1", "2", "3" и т.д. (без ведущего нуля), это может быть курс
                return course
            }
        }
        
        // Пробуем формат "X-XX" или "X-XXX" (например, "2-01", "4-12", "1-01", "Щ-01")
        if (cleanCode.contains("-")) {
            val parts = cleanCode.split("-")
            if (parts.isNotEmpty()) {
                val firstPart = parts[0].trim()
                // Первая часть - это курс (например, "1-01" -> курс 1)
                val course = firstPart.toIntOrNull()
                if (course != null && course > 0 && course <= 4) {
                    return course
                }
                // Если первая часть не число (например, "Щ-01"), пробуем вторую часть
                // НО: вторая часть обычно номер группы, а не курс
                // Для "Щ-01" вторая часть "01" начинается с 0, это номер группы
                // Но если QKURS содержит мусор, можно попробовать извлечь курс из второй цифры "01" -> 1
                if (parts.size > 1) {
                    val secondPart = parts[1].trim()
                    // Если вторая часть - это число без ведущего нуля (1, 2, 3, 4, 5, 6), это может быть курс
                    if (secondPart.length == 1 || (secondPart.length == 2 && !secondPart.startsWith("0"))) {
                        val course2 = secondPart.toIntOrNull()
                        if (course2 != null && course2 > 0 && course2 <= 6) {
                            return course2
                        }
                    }
                    // Если вторая часть "01", "02" и т.д. (с ведущим нулем), пробуем извлечь курс из второй цифры
                    // Это fallback когда QKURS содержит мусор
                    if (secondPart.length == 2 && secondPart.startsWith("0") && secondPart.all { it.isDigit() }) {
                        val secondDigit = secondPart[1].toString().toIntOrNull()
                        if (secondDigit != null && secondDigit > 0 && secondDigit <= 4) {
                            return secondDigit
                        }
                    }
                }
                // Для "Щ-01" курс должен быть определен из других полей (QKURS), но если QKURS мусор, используем fallback выше
            }
        }
        
        // Пробуем извлечь число из начала строки (например, "2П-01" -> 2)
        val numberMatch = Regex("^\\s*(\\d+)").find(cleanCode)
        if (numberMatch != null) {
            val course = numberMatch.groupValues[1].toIntOrNull()
            if (course != null && course > 0 && course <= 6) {
                return course
            }
        }
        
        // Пробуем извлечь число из конца строки (например, "П-4" -> 4)
        val numberMatchEnd = Regex("(\\d+)\\s*$").find(cleanCode)
        if (numberMatchEnd != null) {
            val course = numberMatchEnd.groupValues[1].toIntOrNull()
            if (course != null && course > 0 && course <= 6) {
                return course
            }
        }
        
        // Для кода "01", "02" и т.д. (без дефиса) - это код группы, а не курс
        // Курс должен быть в QKURS или других полях
        return 0
    }

}
