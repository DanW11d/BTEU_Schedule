package com.example.bteu_schedule.domain.service

import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.utils.WeekCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Сервис для работы с расписанием и датами
 * Предоставляет функции для получения следующей пары, расписания на дату и т.д.
 */
class ScheduleService {
    
    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        /**
         * Парсит время из строки формата "HH:mm-HH:mm" или "HH:mm"
         */
        private fun parseTime(timeString: String): LocalTime? {
            return try {
                val timePart = timeString.split("-").firstOrNull()?.trim() ?: timeString.trim()
                LocalTime.parse(timePart, TIME_FORMATTER)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Парсит дату из строки формата "yyyy-MM-dd"
         */
        private fun parseDate(dateString: String): LocalDate? {
            return try {
                LocalDate.parse(dateString, DATE_FORMATTER)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Получить следующую пару для группы
     * @param lessons Список всех занятий группы
     * @param currentDateTime Текущее время (по умолчанию - сейчас)
     * @return Следующее занятие или null, если занятий больше нет
     */
    fun getNextLesson(
        lessons: List<LessonUi>,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): LessonUi? {
        val currentDate = currentDateTime.toLocalDate()
        val currentTime = currentDateTime.toLocalTime()
        val currentDayOfWeek = currentDate.dayOfWeek.value // 1=Monday, 7=Sunday
        
        // Определяем четность текущей недели
        val isCurrentWeekOdd = WeekCalculator.isWeekOdd(currentDate)
        
        // Фильтруем занятия, которые подходят по неделе
        val relevantLessons = lessons.filter { lesson ->
            when (lesson.weekParity.lowercase()) {
                "odd", "нечетная" -> isCurrentWeekOdd
                "even", "четная" -> !isCurrentWeekOdd
                "both", "обе" -> true
                else -> true
            }
        }
        
        // Сортируем занятия по дню недели и времени
        val sortedLessons = relevantLessons.sortedWith(compareBy(
            { it.dayOfWeek },
            { parseTime(it.time) ?: LocalTime.MAX }
        ))
        
        // Ищем следующее занятие
        for (lesson in sortedLessons) {
            val lessonTime = parseTime(lesson.time) ?: continue
            
            // Если занятие сегодня и время еще не прошло
            if (lesson.dayOfWeek == currentDayOfWeek && lessonTime.isAfter(currentTime)) {
                return lesson
            }
            
            // Если занятие в будущем дне
            if (lesson.dayOfWeek > currentDayOfWeek) {
                return lesson
            }
        }
        
        // Если ничего не найдено в текущей неделе, ищем в следующей
        val nextWeekDate = currentDate.plusWeeks(1)
        val isNextWeekOdd = WeekCalculator.isWeekOdd(nextWeekDate)
        
        val nextWeekLessons = lessons.filter { lesson ->
            when (lesson.weekParity.lowercase()) {
                "odd", "нечетная" -> isNextWeekOdd
                "even", "четная" -> !isNextWeekOdd
                "both", "обе" -> true
                else -> true
            }
        }
        
        return nextWeekLessons.minByOrNull { 
            it.dayOfWeek * 1000 + (parseTime(it.time)?.toSecondOfDay() ?: Int.MAX_VALUE)
        }
    }
    
    /**
     * Получить расписание на конкретную дату
     * @param lessons Список всех занятий группы
     * @param date Дата для получения расписания
     * @return Список занятий на указанную дату, отсортированный по времени
     */
    fun getLessonsForDate(
        lessons: List<LessonUi>,
        date: LocalDate
    ): List<LessonUi> {
        val dayOfWeek = date.dayOfWeek.value // 1=Monday, 7=Sunday
        val isWeekOdd = WeekCalculator.isWeekOdd(date)
        
        return lessons
            .filter { lesson ->
                // Проверяем день недели
                lesson.dayOfWeek == dayOfWeek &&
                // Проверяем четность недели
                when (lesson.weekParity.lowercase()) {
                    "odd", "нечетная" -> isWeekOdd
                    "even", "четная" -> !isWeekOdd
                    "both", "обе" -> true
                    else -> true
                }
            }
            .sortedBy { parseTime(it.time) ?: LocalTime.MAX }
    }
    
    /**
     * Получить расписание на сегодня
     */
    fun getTodayLessons(lessons: List<LessonUi>): List<LessonUi> {
        return getLessonsForDate(lessons, LocalDate.now())
    }
    
    /**
     * Получить расписание на завтра
     */
    fun getTomorrowLessons(lessons: List<LessonUi>): List<LessonUi> {
        return getLessonsForDate(lessons, LocalDate.now().plusDays(1))
    }
    
    /**
     * Получить расписание на конкретный день недели
     * @param dayOfWeek 1=Понедельник, 2=Вторник, ..., 6=Суббота
     */
    fun getLessonsForDayOfWeek(
        lessons: List<LessonUi>,
        dayOfWeek: Int,
        isOddWeek: Boolean? = null
    ): List<LessonUi> {
        val weekParity = isOddWeek ?: WeekCalculator.isCurrentWeekOdd()
        
        return lessons
            .filter { lesson ->
                lesson.dayOfWeek == dayOfWeek &&
                when (lesson.weekParity.lowercase()) {
                    "odd", "нечетная" -> weekParity
                    "even", "четная" -> !weekParity
                    "both", "обе" -> true
                    else -> true
                }
            }
            .sortedBy { parseTime(it.time) ?: LocalTime.MAX }
    }
    
    /**
     * Получить расписание на неделю
     * @param lessons Список всех занятий группы
     * @param startDate Начало недели (понедельник)
     * @return Map: день недели -> список занятий
     */
    fun getWeekSchedule(
        lessons: List<LessonUi>,
        startDate: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    ): Map<Int, List<LessonUi>> {
        val isWeekOdd = WeekCalculator.isWeekOdd(startDate)
        
        return (1..6).associateWith { dayOfWeek ->
            lessons
                .filter { lesson ->
                    lesson.dayOfWeek == dayOfWeek &&
                    when (lesson.weekParity.lowercase()) {
                        "odd", "нечетная" -> isWeekOdd
                        "even", "четная" -> !isWeekOdd
                        "both", "обе" -> true
                        else -> true
                    }
                }
                .sortedBy { parseTime(it.time) ?: LocalTime.MAX }
        }
    }
    
    /**
     * Получить название дня недели на русском
     */
    fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            7 -> "Воскресенье"
            else -> "Неизвестно"
        }
    }
}

