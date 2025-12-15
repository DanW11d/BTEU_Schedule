package com.example.bteu_schedule.widget

import com.example.bteu_schedule.domain.models.ExamUi
import com.example.bteu_schedule.domain.models.LessonUi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A7.4: Генератор умных реплик ассистента для виджетов
 * A7.5: Алгоритм выбора сообщения
 * 
 * Виджет всегда выбирает одну главную реплику на основе:
 * - Расписания на сегодня/завтра
 * - Ближайших экзаменов
 * - Текущего времени
 * 
 * A7.5. Приоритет выбора сообщения:
 * 1. Если через 60 минут пара → показать её
 * 2. Если завтра рано → «Завтра в 8:00…»
 * 3. Если сегодня мало пар → «Сегодня только 1 пара»
 * 4. Если нет пар → «Свободный день»
 * 5. Если экзамен скоро → напомнить
 * 6. Иначе → «Могу помочь с расписанием»
 */
object WidgetSmartMessageGenerator {
    
    /**
     * Генерирует главную реплику ассистента для виджета
     * 
     * A7.5: Алгоритм выбора сообщения с приоритетами
     * 
     * @param todayLessons Расписание на сегодня
     * @param tomorrowLessons Расписание на завтра
     * @param upcomingExams Ближайшие экзамены (отсортированные по дате)
     * @param currentTime Текущее время в миллисекундах (опционально, по умолчанию System.currentTimeMillis())
     * @return Главная реплика ассистента
     */
    fun generateSmartMessage(
        todayLessons: List<LessonUi>,
        tomorrowLessons: List<LessonUi>,
        upcomingExams: List<ExamUi>,
        currentTime: Long = System.currentTimeMillis()
    ): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
        }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // A7.5.1: Приоритет 1: Если через 60 минут пара → показать её
        val nextLessonToday = findNextLesson(todayLessons, currentHour, currentMinute)
        if (nextLessonToday != null) {
            val minutesUntilLesson = minutesUntilLesson(nextLessonToday, currentHour, currentMinute)
            if (minutesUntilLesson in 0..60) { // До 60 минут
                return when {
                    minutesUntilLesson < 5 -> "Пара начинается сейчас! 🚀"
                    minutesUntilLesson < 60 -> "Через $minutesUntilLesson минут ${nextLessonToday.subject}, ауд. ${nextLessonToday.classroom} 💻"
                    else -> "Через 1 час ${nextLessonToday.subject}, ауд. ${nextLessonToday.classroom} 💻"
                }
            }
        }
        
        // A7.5.2: Приоритет 2: Если завтра рано → «Завтра в 8:00…»
        val firstLessonTomorrow = tomorrowLessons.firstOrNull()
        if (firstLessonTomorrow != null) {
            val timeString = firstLessonTomorrow.time.split("-").firstOrNull() ?: "9:00"
            val timeParts = timeString.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
            
            // "Рано" = до 9:00 включительно
            if (hour <= 9) {
                return "Завтра в $timeString 🕗"
            }
        }
        
        // A7.5.3: Приоритет 3: Если сегодня мало пар → «Сегодня только 1 пара»
        if (todayLessons.size == 1) {
            return "Сегодня только 1 пара 🌿"
        }
        
        // A7.5.4: Приоритет 4: Если нет пар → «Свободный день»
        if (todayLessons.isEmpty()) {
            return "Свободный день 🎉"
        }
        
        // A7.5.5: Приоритет 5: Если экзамен скоро → напомнить
        val nearestExam = upcomingExams.firstOrNull()
        if (nearestExam != null) {
            val examDate = parseExamDate(nearestExam.date)
            if (examDate != null) {
                val daysUntilExam = daysBetween(calendar, examDate)
                if (daysUntilExam in 0..7) { // До 7 дней
                    return when (daysUntilExam) {
                        0 -> "Экзамен сегодня. Готов? 📚"
                        1 -> "Экзамен завтра. Готов? 📚"
                        2 -> "Экзамен через 2 дня. Готов? 📚"
                        3 -> "Экзамен через 3 дня. Готов? 📚"
                        else -> "Экзамен через $daysUntilExam дней. Готов? 📚"
                    }
                }
            }
        }
        
        // A7.5.6: Иначе → «Могу помочь с расписанием»
        return "Могу помочь с расписанием 💬"
    }
    
    
    /**
     * Парсит дату экзамена из строки формата "yyyy-MM-dd"
     */
    private fun parseExamDate(dateString: String): Calendar? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return null
            Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Находит следующую пару на сегодня
     */
    private fun findNextLesson(
        lessons: List<LessonUi>,
        currentHour: Int,
        currentMinute: Int
    ): LessonUi? {
        val currentTimeMinutes = currentHour * 60 + currentMinute
        
        return lessons
            .sortedBy { it.pairNumber }
            .firstOrNull { lesson ->
                val lessonStartTime = parseTime(lesson.time.split("-").firstOrNull() ?: "")
                lessonStartTime != null && lessonStartTime > currentTimeMinutes
            }
    }
    
    /**
     * Вычисляет количество минут до начала пары
     */
    private fun minutesUntilLesson(
        lesson: LessonUi,
        currentHour: Int,
        currentMinute: Int
    ): Int {
        val currentTimeMinutes = currentHour * 60 + currentMinute
        val lessonStartTime = parseTime(lesson.time.split("-").firstOrNull() ?: "")
            ?: return Int.MAX_VALUE
        
        return lessonStartTime - currentTimeMinutes
    }
    
    /**
     * Парсит время в формате "HH:mm" в минуты от начала дня
     */
    private fun parseTime(timeString: String): Int? {
        val parts = timeString.split(":")
        if (parts.size != 2) return null
        
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        
        return hours * 60 + minutes
    }
    
    /**
     * Вычисляет количество дней между двумя датами
     */
    private fun daysBetween(start: Calendar, end: Calendar): Int {
        val startDay = Calendar.getInstance().apply {
            time = start.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endDay = Calendar.getInstance().apply {
            time = end.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diffInMillis = endDay.timeInMillis - startDay.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

