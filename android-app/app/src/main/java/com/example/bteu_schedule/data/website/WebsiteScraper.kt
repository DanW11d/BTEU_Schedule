package com.example.bteu_schedule.data.website

import android.util.Log
import com.example.bteu_schedule.data.config.AppConfig
import com.example.bteu_schedule.domain.models.BellScheduleUi
import com.example.bteu_schedule.domain.models.DepartmentUi
import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

/**
 * Парсер сайта БТЭУ для извлечения данных о расписании
 * 
 * URL сайта: https://bteu.by/studentu/obrazovanie-i-praktika/raspisanie-zanyatij/
 */
object WebsiteScraper {
    
    private const val TAG = "WebsiteScraper"
    private const val BASE_URL = "https://bteu.by"
    private const val SCHEDULE_BASE_URL = "$BASE_URL/studentu/obrazovanie-i-praktika/raspisanie-zanyatij"
    
    // URL страниц расписания по факультетам
    private const val FEU_STABLE_SCHEDULE = "$SCHEDULE_BASE_URL/stabilnoe-raspisanie-feu/"
    private const val FKIF_STABLE_SCHEDULE = "$SCHEDULE_BASE_URL/stabilnoe-raspisanie-fkif/"
    
    // Таймаут для запросов (30 секунд)
    private const val TIMEOUT_MS = 30000
    
    /**
     * Получить список факультетов
     */
    fun getFaculties(): List<FacultyUi> {
        return listOf(
            FacultyUi(
                id = 1,
                code = "FEU",
                name = "Факультет экономики и управления",
                description = "Факультет экономики и управления"
            ),
            FacultyUi(
                id = 2,
                code = "FKIF",
                name = "Факультет коммерции и финансов",
                description = "Факультет коммерции и финансов"
            )
        )
    }
    
    /**
     * Получить список групп для факультета
     * Парсит страницу стабильного расписания и извлекает список групп
     */
    suspend fun getGroups(facultyCode: String): List<GroupUi> {
        return try {
            val scheduleUrl = when (facultyCode.uppercase()) {
                "FEU" -> FEU_STABLE_SCHEDULE
                "FKIF" -> FKIF_STABLE_SCHEDULE
                else -> return emptyList()
            }
            
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Парсинг групп с URL: $scheduleUrl")
            }
            
            val doc = Jsoup.connect(scheduleUrl)
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get()
            
            val groups = mutableListOf<GroupUi>()
            
            // Ищем ссылки на расписание групп
            // Обычно группы представлены в виде ссылок или таблиц
            val links = doc.select("a[href*='raspisanie'], a[href*='group'], a[href*='групп']")
            
            for (link in links) {
                val href = link.attr("href")
                val text = link.text().trim()
                
                // Извлекаем код группы из текста или href
                val groupCode = extractGroupCode(text, href)
                if (groupCode != null && groupCode.isNotBlank()) {
                    // Извлекаем курс из кода группы
                    val course = extractCourseFromGroupCode(groupCode)
                    
                    groups.add(
                        GroupUi(
                            code = groupCode,
                            name = text.ifBlank { "Группа $groupCode" },
                            course = course,
                            specialization = "",
                            facultyCode = facultyCode,
                            facultyName = when (facultyCode.uppercase()) {
                                "FEU" -> "Факультет экономики и управления"
                                "FKIF" -> "Факультет коммерции и финансов"
                                else -> ""
                            },
                            educationForm = "full_time", // По умолчанию очная форма
                            departmentId = null,
                            departmentName = null
                        )
                    )
                }
            }
            
            // Если не нашли через ссылки, пробуем парсить таблицы
            if (groups.isEmpty()) {
                val tables = doc.select("table")
                for (table in tables) {
                    val rows = table.select("tr")
                    for (row in rows) {
                        val cells = row.select("td, th")
                        for (cell in cells) {
                            val text = cell.text().trim()
                            val groupCode = extractGroupCode(text, "")
                            if (groupCode != null && groupCode.isNotBlank()) {
                                val course = extractCourseFromGroupCode(groupCode)
                                groups.add(
                                    GroupUi(
                                        code = groupCode,
                                        name = "Группа $groupCode",
                                        course = course,
                                        specialization = "",
                                        facultyCode = facultyCode,
                                        facultyName = when (facultyCode.uppercase()) {
                                            "FEU" -> "Факультет экономики и управления"
                                            "FKIF" -> "Факультет коммерции и финансов"
                                            else -> ""
                                        },
                                        educationForm = "full_time",
                                        departmentId = null,
                                        departmentName = null
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Удаляем дубликаты по коду группы
            val uniqueGroups = groups.distinctBy { it.code }
            
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Найдено групп: ${uniqueGroups.size} для факультета $facultyCode")
            }
            
            uniqueGroups
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при парсинге групп: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка при парсинге групп: ${e.message}", e)
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
    ): List<LessonUi> {
        return try {
            val scheduleUrl = when (facultyCode.uppercase()) {
                "FEU" -> FEU_STABLE_SCHEDULE
                "FKIF" -> FKIF_STABLE_SCHEDULE
                else -> return emptyList()
            }
            
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Парсинг расписания для группы $groupCode с URL: $scheduleUrl")
            }
            
            val doc = Jsoup.connect(scheduleUrl)
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get()
            
            val lessons = mutableListOf<LessonUi>()
            
            // Ищем таблицу с расписанием для конкретной группы
            // Обычно расписание представлено в виде таблиц
            val tables = doc.select("table")
            
            for (table in tables) {
                // Проверяем, содержит ли таблица нужную группу
                val tableText = table.text()
                if (tableText.contains(groupCode, ignoreCase = true)) {
                    val parsedLessons = parseScheduleTable(table, groupCode, dayOfWeek, weekParity)
                    lessons.addAll(parsedLessons)
                }
            }
            
            // Если не нашли в таблицах, пробуем найти по ссылкам
            if (lessons.isEmpty()) {
                // Ищем ссылки, содержащие код группы в href или тексте
                val groupLinks = doc.select("a[href*='$groupCode']")
                // Также ищем ссылки, содержащие код группы в тексте
                val textLinks = doc.select("a").filter { it.text().contains(groupCode, ignoreCase = true) }
                // Объединяем и удаляем дубликаты по href
                val allLinks = mutableListOf<Element>()
                val seenHrefs = mutableSetOf<String>()
                for (link in groupLinks) {
                    val href = link.attr("href")
                    if (href !in seenHrefs) {
                        allLinks.add(link)
                        seenHrefs.add(href)
                    }
                }
                for (link in textLinks) {
                    val href = link.attr("href")
                    if (href !in seenHrefs) {
                        allLinks.add(link)
                        seenHrefs.add(href)
                    }
                }
                
                for (link in allLinks) {
                    val href = link.attr("href")
                    val fullUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                    
                    try {
                        val groupDoc = Jsoup.connect(fullUrl)
                            .timeout(TIMEOUT_MS)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .get()
                        
                        val groupTables = groupDoc.select("table")
                        for (table in groupTables) {
                            val parsedLessons = parseScheduleTable(table, groupCode, dayOfWeek, weekParity)
                            lessons.addAll(parsedLessons)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Ошибка при парсинге страницы группы $fullUrl: ${e.message}")
                    }
                }
            }
            
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Найдено занятий: ${lessons.size} для группы $groupCode")
            }
            
            lessons
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при парсинге расписания: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка при парсинге расписания: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Парсит таблицу расписания
     */
    private fun parseScheduleTable(
        table: Element,
        groupCode: String,
        dayOfWeek: Int?,
        weekParity: String?
    ): List<LessonUi> {
        val lessons = mutableListOf<LessonUi>()
        val rows = table.select("tr")
        
        var currentDay: Int? = null
        var currentWeekParity: String? = null
        
        for (row in rows) {
            val cells = row.select("td, th")
            if (cells.isEmpty()) continue
            
            // Пытаемся определить день недели из заголовка строки
            val rowText = row.text()
            val day = extractDayOfWeek(rowText)
            if (day != null) {
                currentDay = day
            }
            
            // Пытаемся определить четность недели
            val parity = extractWeekParity(rowText)
            if (parity != null) {
                currentWeekParity = parity
            }
            
            // Если указан день недели для фильтрации, пропускаем другие дни
            if (dayOfWeek != null && currentDay != null && currentDay != dayOfWeek) {
                continue
            }
            
            // Если указана четность недели для фильтрации, пропускаем другие недели
            if (weekParity != null && currentWeekParity != null && 
                currentWeekParity.lowercase() != weekParity.lowercase()) {
                continue
            }
            
            // Парсим ячейки таблицы
            val cellsSize = cells.size
            if (cellsSize >= 3) {
                try {
                    // Обычно структура: номер пары | предмет | преподаватель | аудитория
                    val firstCellText = cells.get(0).text()
                    val pairNumber = extractLessonNumber(firstCellText)
                    val subject = if (cellsSize > 1) cells.get(1).text().trim() else ""
                    val teacher = if (cellsSize > 2) cells.get(2).text().trim() else ""
                    val classroom = if (cellsSize > 3) cells.get(3).text().trim() else ""
                    val typeCellText = if (cellsSize > 4) cells.get(4).text() else ""
                    val type = extractLessonType(typeCellText)
                    
                    if (pairNumber != null && subject.isNotBlank()) {
                        // Формируем время из расписания звонков (если доступно)
                        val bellSchedule = getStaticBellSchedule()
                        val bellScheduleItem = bellSchedule.find { it.lessonNumber == pairNumber }
                        val time = if (bellScheduleItem != null && 
                                       bellScheduleItem.lessonStart != null && 
                                       bellScheduleItem.lessonEnd != null) {
                            "${bellScheduleItem.lessonStart}-${bellScheduleItem.lessonEnd}"
                        } else {
                            ""
                        }
                        
                        lessons.add(
                            LessonUi(
                                id = 0,
                                pairNumber = pairNumber,
                                dayOfWeek = currentDay ?: dayOfWeek ?: 1,
                                time = time,
                                subject = subject,
                                teacher = teacher,
                                classroom = classroom,
                                type = type,
                                weekParity = currentWeekParity ?: weekParity ?: "both"
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Ошибка при парсинге строки таблицы: ${e.message}")
                }
            }
        }
        
        return lessons
    }
    
    /**
     * Извлекает код группы из текста или URL
     */
    private fun extractGroupCode(text: String, href: String): String? {
        // Паттерны для кодов групп: S-41, П-31, А-04, 1-25 и т.д.
        val patterns = listOf(
            Regex("([А-ЯA-ZЁ][А-ЯA-ZЁа-яa-zё]*-?\\d{1,2})"), // S-41, П-31
            Regex("(\\d{1,2}-\\d{1,2})"), // 1-25, 4-12
            Regex("(группа\\s+([А-ЯA-ZЁ]?\\d{1,2}(?:-\\d{1,2})?))", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val code = match.groupValues.lastOrNull()?.trim()
                if (code != null && code.isNotBlank()) {
                    return code
                }
            }
        }
        
        // Пробуем извлечь из href
        if (href.isNotBlank()) {
            for (pattern in patterns) {
                val match = pattern.find(href)
                if (match != null) {
                    val code = match.groupValues.lastOrNull()?.trim()
                    if (code != null && code.isNotBlank()) {
                        return code
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Извлекает курс из кода группы
     */
    private fun extractCourseFromGroupCode(code: String): Int {
        // Паттерны: S-41 -> 4, П-31 -> 3, 1-25 -> 1
        val firstDigitMatch = Regex("(\\d)").find(code)
        if (firstDigitMatch != null) {
            val course = firstDigitMatch.groupValues.getOrNull(1)?.toIntOrNull()
            if (course != null && course in 1..4) {
                return course
            }
        }
        return 0
    }
    
    /**
     * Извлекает день недели из текста
     */
    private fun extractDayOfWeek(text: String): Int? {
        val dayNames = mapOf(
            "понедельник" to 1,
            "вторник" to 2,
            "среда" to 3,
            "четверг" to 4,
            "пятница" to 5,
            "суббота" to 6,
            "пн" to 1,
            "вт" to 2,
            "ср" to 3,
            "чт" to 4,
            "пт" to 5,
            "сб" to 6
        )
        
        val lowerText = text.lowercase()
        for ((name, day) in dayNames) {
            if (lowerText.contains(name)) {
                return day
            }
        }
        
        // Пробуем извлечь число от 1 до 6
        val numberMatch = Regex("(\\d)").find(text)
        if (numberMatch != null) {
            val day = numberMatch.groupValues.getOrNull(1)?.toIntOrNull()
            if (day != null && day in 1..6) {
                return day
            }
        }
        
        return null
    }
    
    /**
     * Извлекает четность недели из текста
     */
    private fun extractWeekParity(text: String): String? {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("нечетн") || lowerText.contains("нечётн") || lowerText.contains("odd") -> "odd"
            lowerText.contains("четн") || lowerText.contains("чётн") || lowerText.contains("even") -> "even"
            else -> null
        }
    }
    
    /**
     * Извлекает номер пары из текста
     */
    private fun extractLessonNumber(text: String): Int? {
        val numberMatch = Regex("(\\d+)").find(text)
        return numberMatch?.groupValues?.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..7 }
    }
    
    /**
     * Извлекает тип занятия из текста
     */
    private fun extractLessonType(text: String): String {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("лекц") -> "lecture"
            lowerText.contains("практ") -> "practice"
            lowerText.contains("лаб") -> "lab"
            lowerText.contains("семинар") -> "seminar"
            else -> "lecture"
        }
    }
    
    /**
     * Получить расписание звонков с сайта БТЭУ
     * Парсит главную страницу расписания и извлекает расписание звонков
     */
    suspend fun getBellSchedule(): List<BellScheduleUi> {
        return try {
            if (AppConfig.LOG_API_REQUESTS) {
                Log.d(TAG, "Парсинг расписания звонков с URL: $SCHEDULE_BASE_URL/")
            }
            
            val doc = Jsoup.connect("$SCHEDULE_BASE_URL/")
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get()
            
            val bellSchedule = mutableListOf<BellScheduleUi>()
            
            // Ищем блок с расписанием звонков на странице
            // Обычно он находится в тексте "Расписание звонков" или в таблице
            val scheduleText = doc.text()
            
            // Парсим расписание звонков из текста
            // Формат: "1 пара — 9:00-9:45, 9:50-10:35"
            val bellSchedulePattern = Regex("""(\d+)\s+пара\s*[—–-]\s*(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2}),\s*(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2})""")
            val matches = bellSchedulePattern.findAll(scheduleText)
            
            for (match in matches) {
                val lessonNumber = match.groupValues.getOrNull(1)?.toIntOrNull() ?: continue
                val startHour1 = match.groupValues.getOrNull(2)?.toIntOrNull() ?: continue
                val startMin1 = match.groupValues.getOrNull(3)?.toIntOrNull() ?: continue
                val endHour1 = match.groupValues.getOrNull(4)?.toIntOrNull() ?: continue
                val endMin1 = match.groupValues.getOrNull(5)?.toIntOrNull() ?: continue
                val startHour2 = match.groupValues.getOrNull(6)?.toIntOrNull() ?: continue
                val startMin2 = match.groupValues.getOrNull(7)?.toIntOrNull() ?: continue
                val endHour2 = match.groupValues.getOrNull(8)?.toIntOrNull() ?: continue
                val endMin2 = match.groupValues.getOrNull(9)?.toIntOrNull() ?: continue
                
                // Формируем время начала и конца пары
                val lessonStart = String.format("%02d:%02d", startHour1, startMin1)
                val lessonEnd = String.format("%02d:%02d", endHour2, endMin2)
                
                bellSchedule.add(
                    BellScheduleUi(
                        lessonNumber = lessonNumber,
                        lessonStart = lessonStart,
                        lessonEnd = lessonEnd,
                        breakTimeMinutes = 5, // Перерыв между частями пары
                        breakAfterLessonMinutes = calculateBreakAfterLesson(lessonNumber),
                        description = "Пара $lessonNumber"
                    )
                )
            }
            
            // Если не нашли через парсинг, используем статическое расписание
            if (bellSchedule.isEmpty()) {
                if (AppConfig.LOG_API_REQUESTS) {
                    Log.d(TAG, "Расписание звонков не найдено на сайте, используем статическое")
                }
                return getStaticBellSchedule()
            }
            
            // Сортируем по номеру пары и возвращаем
            return bellSchedule.sortedBy { it.lessonNumber }
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при парсинге расписания звонков: ${e.message}", e)
            // Возвращаем статическое расписание при ошибке
            getStaticBellSchedule()
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка при парсинге расписания звонков: ${e.message}", e)
            // Возвращаем статическое расписание при ошибке
            getStaticBellSchedule()
        }
    }
    
    /**
     * Статическое расписание звонков БТЭУ (из предоставленных данных)
     */
    fun getStaticBellSchedule(): List<BellScheduleUi> {
        return listOf(
            BellScheduleUi(
                lessonNumber = 1,
                lessonStart = "09:00",
                lessonEnd = "10:35",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 15,
                description = "1 пара"
            ),
            BellScheduleUi(
                lessonNumber = 2,
                lessonStart = "10:50",
                lessonEnd = "12:25",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 30,
                description = "2 пара"
            ),
            BellScheduleUi(
                lessonNumber = 3,
                lessonStart = "12:55",
                lessonEnd = "14:30",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 10,
                description = "3 пара"
            ),
            BellScheduleUi(
                lessonNumber = 4,
                lessonStart = "14:40",
                lessonEnd = "16:15",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 10,
                description = "4 пара"
            ),
            BellScheduleUi(
                lessonNumber = 5,
                lessonStart = "16:25",
                lessonEnd = "18:00",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 30,
                description = "5 пара"
            ),
            BellScheduleUi(
                lessonNumber = 6,
                lessonStart = "18:30",
                lessonEnd = "20:05",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 10,
                description = "6 пара"
            ),
            BellScheduleUi(
                lessonNumber = 7,
                lessonStart = "20:15",
                lessonEnd = "21:50",
                breakTimeMinutes = 5,
                breakAfterLessonMinutes = 0,
                description = "7 пара"
            )
        )
    }
    
    /**
     * Вычисляет перерыв после пары в минутах
     */
    private fun calculateBreakAfterLesson(lessonNumber: Int): Int {
        return when (lessonNumber) {
            1 -> 15  // После 1 пары до 2 пары: 10:35 -> 10:50 = 15 минут
            2 -> 30  // После 2 пары до 3 пары: 12:25 -> 12:55 = 30 минут
            3 -> 10  // После 3 пары до 4 пары: 14:30 -> 14:40 = 10 минут
            4 -> 10  // После 4 пары до 5 пары: 16:15 -> 16:25 = 10 минут
            5 -> 30  // После 5 пары до 6 пары: 18:00 -> 18:30 = 30 минут
            6 -> 10  // После 6 пары до 7 пары: 20:05 -> 20:15 = 10 минут
            7 -> 0   // После 7 пары - конец дня
            else -> 10
        }
    }
    
    /**
     * Получить список всех кафедр БТЭУ
     * Возвращает статические данные о кафедрах
     */
    fun getDepartments(): List<DepartmentUi> {
        return getStaticDepartments()
    }
    
    /**
     * Статические данные о кафедрах БТЭУ
     */
    fun getStaticDepartments(): List<DepartmentUi> {
        var id = 1
        return listOf(
            // Факультет экономики и управления (FEU)
            DepartmentUi(
                id = id++,
                code = "IVCS",
                name = "Кафедра информационно-вычислительных систем",
                facultyCode = "FEU",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "PRET",
                name = "Кафедра права и экономических теорий",
                facultyCode = "FEU",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "MNE",
                name = "Кафедра мировой и национальной экономики",
                facultyCode = "FEU",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "ET",
                name = "Кафедра экономики торговли",
                facultyCode = "FEU",
                description = null
            ),
            
            // Факультет коммерции и финансов (FKIF)
            DepartmentUi(
                id = id++,
                code = "BAF",
                name = "Кафедра бухгалтерского учета и финансов",
                facultyCode = "FKIF",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "HPE",
                name = "Кафедра гуманитарного и физического воспитания",
                facultyCode = "FKIF",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "CL",
                name = "Кафедра коммерции и логистики",
                facultyCode = "FKIF",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "M",
                name = "Кафедра маркетинга",
                facultyCode = "FKIF",
                description = null
            ),
            DepartmentUi(
                id = id++,
                code = "T",
                name = "Кафедра товароведения",
                facultyCode = "FKIF",
                description = null
            ),
            
            // Факультет повышения квалификации и переподготовки (FPKP)
            DepartmentUi(
                id = id++,
                code = "ELD",
                name = "Кафедра экономических и правовых дисциплин",
                facultyCode = "FPKP",
                description = null
            )
        )
    }
}

