package com.example.bteu_schedule.utils

import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Сервис для экспорта расписания в формат iCalendar (.ics)
 * Формат RFC 5545
 */
object CalendarExportService {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    
    /**
     * Генерирует .ics файл из списка занятий
     * 
     * @param lessons Список занятий для экспорта
     * @param group Группа (для названия календаря)
     * @param startDate Дата начала периода экспорта
     * @param endDate Дата окончания периода экспорта
     * @return Строка в формате .ics
     */
    fun generateIcsFile(
        lessons: List<LessonUi>,
        group: GroupUi,
        startDate: Date,
        endDate: Date
    ): String {
        val calendar = StringBuilder()
        
        // Заголовок iCalendar
        calendar.appendLine("BEGIN:VCALENDAR")
        calendar.appendLine("VERSION:2.0")
        calendar.appendLine("PRODID:-//BTEU Schedule//Schedule Export//RU")
        calendar.appendLine("CALSCALE:GREGORIAN")
        calendar.appendLine("METHOD:PUBLISH")
        calendar.appendLine("X-WR-CALNAME:Расписание ${group.code}")
        calendar.appendLine("X-WR-CALDESC:Расписание занятий группы ${group.code}")
        calendar.appendLine("X-WR-TIMEZONE:Europe/Minsk")
        
        // Генерируем события для каждого занятия
        lessons.forEach { lesson ->
            calendar.append(generateEvent(lesson, group, startDate, endDate))
        }
        
        calendar.appendLine("END:VCALENDAR")
        
        return calendar.toString()
    }
    
    /**
     * Генерирует одно событие (VEVENT) для занятия
     */
    private fun generateEvent(
        lesson: LessonUi,
        group: GroupUi,
        startDate: Date,
        endDate: Date
    ): String {
        val event = StringBuilder()
        
        event.appendLine("BEGIN:VEVENT")
        
        // Уникальный ID события
        event.appendLine("UID:${group.code}-${lesson.id}-${System.currentTimeMillis()}@bteu-schedule")
        
        // Дата создания
        event.appendLine("DTSTAMP:${dateFormat.format(Date())}")
        
        // Название события
        val summary = buildSummary(lesson)
        event.appendLine("SUMMARY:${escapeText(summary)}")
        
        // Описание
        val description = buildDescription(lesson, group)
        event.appendLine("DESCRIPTION:${escapeText(description)}")
        
        // Местоположение
        if (lesson.classroom.isNotBlank()) {
            event.appendLine("LOCATION:${escapeText(lesson.classroom)}")
        }
        
        // Время начала и окончания (для первого вхождения)
        val (startDateTime, endDateTime) = calculateDateTime(lesson, startDate)
        if (startDateTime != null && endDateTime != null) {
            event.appendLine("DTSTART:${dateFormat.format(startDateTime)}")
            event.appendLine("DTEND:${dateFormat.format(endDateTime)}")
        }
        
        // Правило повторения (RRULE) для еженедельных занятий
        val rrule = buildRRule(lesson, startDate, endDate)
        if (rrule != null) {
            event.appendLine("RRULE:$rrule")
        }
        
        // Категория (тип занятия)
        val category = when (lesson.type.lowercase()) {
            "lecture" -> "Лекция"
            "practice" -> "Практика"
            "laboratory" -> "Лабораторная"
            else -> "Занятие"
        }
        event.appendLine("CATEGORIES:$category")
        
        // Приоритет (необязательно)
        event.appendLine("PRIORITY:5")
        
        // Статус
        event.appendLine("STATUS:CONFIRMED")
        
        // Прозрачность (BUSY означает, что время занято)
        event.appendLine("TRANSP:OPAQUE")
        
        event.appendLine("END:VEVENT")
        
        return event.toString()
    }
    
    /**
     * Строит название события
     */
    private fun buildSummary(lesson: LessonUi): String {
        val typePrefix = when (lesson.type.lowercase()) {
            "lecture" -> "[Лекция]"
            "practice" -> "[Практика]"
            "laboratory" -> "[Лаб. работа]"
            else -> ""
        }
        return "$typePrefix ${lesson.subject}".trim()
    }
    
    /**
     * Строит описание события
     */
    private fun buildDescription(lesson: LessonUi, group: GroupUi): String {
        val desc = StringBuilder()
        desc.append("Группа: ${group.code}\n")
        desc.append("Пара: ${lesson.pairNumber}\n")
        desc.append("Время: ${lesson.time}\n")
        
        if (lesson.teacher.isNotBlank()) {
            desc.append("Преподаватель: ${lesson.teacher}\n")
        }
        
        if (lesson.classroom.isNotBlank()) {
            desc.append("Аудитория: ${lesson.classroom}\n")
        }
        
        val weekParityText = when (lesson.weekParity.lowercase()) {
            "odd" -> "Нечётная неделя"
            "even" -> "Чётная неделя"
            "both" -> "Каждую неделю"
            else -> ""
        }
        if (weekParityText.isNotBlank()) {
            desc.append("Неделя: $weekParityText")
        }
        
        return desc.toString()
    }
    
    /**
     * Вычисляет дату и время начала и окончания занятия
     */
    private fun calculateDateTime(lesson: LessonUi, baseDate: Date): Pair<Date?, Date?> {
        // Безопасная проверка времени
        if (lesson.time.isBlank()) return Pair(null, null)
        
        val timeParts = try {
            lesson.time.split("-")
        } catch (e: Exception) {
            android.util.Log.e("CalendarExportService", "Ошибка парсинга времени: ${lesson.time}", e)
            return Pair(null, null)
        }
        
        if (timeParts.size != 2) return Pair(null, null)
        
        val startTime = timeParts[0].trim()
        val endTime = timeParts[1].trim()
        
        if (startTime.isBlank() || endTime.isBlank()) return Pair(null, null)
        
        val startTimeParts = try {
            startTime.split(":")
        } catch (e: Exception) {
            android.util.Log.e("CalendarExportService", "Ошибка парсинга startTime: $startTime", e)
            return Pair(null, null)
        }
        
        val endTimeParts = try {
            endTime.split(":")
        } catch (e: Exception) {
            android.util.Log.e("CalendarExportService", "Ошибка парсинга endTime: $endTime", e)
            return Pair(null, null)
        }
        
        if (startTimeParts.size != 2 || endTimeParts.size != 2) return Pair(null, null)
        
        try {
            val startHour = startTimeParts[0].toIntOrNull() ?: return Pair(null, null)
            val startMinute = startTimeParts[1].toIntOrNull() ?: return Pair(null, null)
            val endHour = endTimeParts[0].toIntOrNull() ?: return Pair(null, null)
            val endMinute = endTimeParts[1].toIntOrNull() ?: return Pair(null, null)
            
            // Валидация значений
            if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59 ||
                endHour < 0 || endHour > 23 || endMinute < 0 || endMinute > 59) {
                android.util.Log.w("CalendarExportService", "Некорректное время: $startHour:$startMinute - $endHour:$endMinute")
                return Pair(null, null)
            }
            
            // Определяем день недели занятия (1 = Понедельник, 6 = Суббота)
            val targetDayOfWeek = convertDayOfWeek(lesson.dayOfWeek)
            
            // Находим ближайшую дату с нужным днем недели от baseDate
            val calendar = Calendar.getInstance().apply {
                time = baseDate
                // Переходим к нужному дню недели
                val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
                var daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7
                if (daysToAdd == 0 && get(Calendar.HOUR_OF_DAY) > startHour) {
                    // Если уже прошло время сегодня, берем следующую неделю
                    daysToAdd = 7
                }
                add(Calendar.DAY_OF_YEAR, daysToAdd)
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
            }
            
            val startDateTime = calendar.time
            
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, endHour)
                set(Calendar.MINUTE, endMinute)
            }
            val endDateTime = calendar.time
            
            return Pair(startDateTime, endDateTime)
        } catch (e: Exception) {
            return Pair(null, null)
        }
    }
    
    /**
     * Преобразует день недели из формата 1-6 (ПН-СБ) в Calendar.DAY_OF_WEEK
     */
    private fun convertDayOfWeek(dayOfWeek: Int): Int {
        // Наш формат: 1=ПН, 2=ВТ, 3=СР, 4=ЧТ, 5=ПТ, 6=СБ
        // Calendar: 2=MONDAY, 3=TUESDAY, 4=WEDNESDAY, 5=THURSDAY, 6=FRIDAY, 7=SATURDAY
        return when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
    }
    
    /**
     * Строит правило повторения (RRULE) для занятия
     */
    private fun buildRRule(lesson: LessonUi, startDate: Date, endDate: Date): String? {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val endDateStr = dateOnlyFormat.format(endDate)
        val dayOfWeekAbbr = getDayOfWeekAbbreviation(convertDayOfWeek(lesson.dayOfWeek))
        
        // Определяем интервал повторения в зависимости от четности недели
        when (lesson.weekParity.lowercase()) {
            "both" -> {
                // Каждую неделю
                return "FREQ=WEEKLY;BYDAY=$dayOfWeekAbbr;INTERVAL=1;UNTIL=${endDateStr}"
            }
            "odd" -> {
                // Нечётные недели (каждые 2 недели, начиная с нечётной)
                return "FREQ=WEEKLY;BYDAY=$dayOfWeekAbbr;INTERVAL=2;UNTIL=${endDateStr}"
            }
            "even" -> {
                // Чётные недели (каждые 2 недели, начиная с чётной)
                // Для чётных недель нужно сдвинуть на 1 неделю от startDate
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val evenStartDate = dateOnlyFormat.format(calendar.time)
                return "FREQ=WEEKLY;BYDAY=$dayOfWeekAbbr;INTERVAL=2;UNTIL=${endDateStr};DTSTART=$evenStartDate"
            }
        }
        
        return null
    }
    
    /**
     * Преобразует день недели Calendar в аббревиатуру для RRULE
     */
    private fun getDayOfWeekAbbreviation(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "MO"
            Calendar.TUESDAY -> "TU"
            Calendar.WEDNESDAY -> "WE"
            Calendar.THURSDAY -> "TH"
            Calendar.FRIDAY -> "FR"
            Calendar.SATURDAY -> "SA"
            Calendar.SUNDAY -> "SU"
            else -> "MO"
        }
    }
    
    /**
     * Экранирует специальные символы в тексте для iCalendar
     */
    private fun escapeText(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
            .replace("\r", "")
    }
    
    /**
     * Получает расписание на неделю для экспорта
     * Объединяет занятия для нечётной и чётной недель
     */
    suspend fun getWeekScheduleForExport(
        repository: com.example.bteu_schedule.data.repository.ScheduleRepository,
        groupCode: String
    ): List<LessonUi> {
        val oddWeek = repository.getWeekSchedule(groupCode, "odd")
        val evenWeek = repository.getWeekSchedule(groupCode, "even")
        
        val allLessons = mutableListOf<LessonUi>()
        val oddLessons = mutableListOf<LessonUi>()
        
        if (oddWeek is ApiResponse.Success) {
            oddLessons.addAll(oddWeek.data)
            allLessons.addAll(oddWeek.data)
        }
        
        if (evenWeek is ApiResponse.Success) {
            // Добавляем только те занятия, которых нет в нечётной неделе
            val oddLessonIds = oddLessons.map { it.id }.toSet()
            allLessons.addAll(evenWeek.data.filter { it.id !in oddLessonIds })
        }
        
        return allLessons
    }
}

