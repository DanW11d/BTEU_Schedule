package com.example.bteu_schedule.utils

import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import java.text.SimpleDateFormat
import java.util.*

/**
 * Сервис для экспорта расписания в Excel/CSV формат
 */
object ExcelExportService {
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("ru", "RU"))
    
    /**
     * Генерирует CSV файл из списка занятий
     * CSV формат легко открывается в Excel
     * 
     * @param lessons Список занятий для экспорта
     * @param group Группа (для заголовка)
     * @return Строка в формате CSV
     */
    fun generateCsvFile(
        lessons: List<LessonUi>,
        group: GroupUi
    ): String {
        val csv = StringBuilder()
        
        // BOM для правильного отображения кириллицы в Excel
        csv.append("\uFEFF")
        
        // Заголовок
        csv.appendLine("Расписание группы ${group.code}")
        csv.appendLine("Специализация: ${group.specialization}")
        csv.appendLine("")
        
        // Заголовки столбцов
        csv.appendLine("День недели;Пара;Время;Предмет;Тип;Преподаватель;Аудитория;Неделя")
        
        // Сортируем занятия по дню недели и номеру пары
        val sortedLessons = lessons.sortedWith(
            compareBy<LessonUi> { it.dayOfWeek }
                .thenBy { it.pairNumber }
        )
        
        // Данные
        sortedLessons.forEach { lesson ->
            val dayName = getDayName(lesson.dayOfWeek)
            val weekParity = when (lesson.weekParity.lowercase()) {
                "odd" -> "Нечётная"
                "even" -> "Чётная"
                "both" -> "Каждую неделю"
                else -> ""
            }
            
            val type = when (lesson.type.lowercase()) {
                "lecture" -> "Лекция"
                "practice" -> "Практика"
                "laboratory" -> "Лабораторная"
                else -> lesson.type
            }
            
            csv.appendLine(
                "${escapeCsv(dayName)};" +
                "${lesson.pairNumber};" +
                "${escapeCsv(lesson.time)};" +
                "${escapeCsv(lesson.subject)};" +
                "${escapeCsv(type)};" +
                "${escapeCsv(lesson.teacher)};" +
                "${escapeCsv(lesson.classroom)};" +
                "${escapeCsv(weekParity)}"
            )
        }
        
        return csv.toString()
    }
    
    /**
     * Генерирует Excel-совместимый CSV с разделителем запятой
     * (альтернативный формат для лучшей совместимости)
     */
    fun generateExcelCsvFile(
        lessons: List<LessonUi>,
        group: GroupUi
    ): String {
        val csv = StringBuilder()
        
        // BOM для правильного отображения кириллицы в Excel
        csv.append("\uFEFF")
        
        // Заголовок
        csv.appendLine("Расписание группы ${group.code}")
        csv.appendLine("Специализация: ${group.specialization}")
        csv.appendLine("")
        
        // Заголовки столбцов (с запятой для Excel)
        csv.appendLine("День недели,Пара,Время,Предмет,Тип,Преподаватель,Аудитория,Неделя")
        
        // Сортируем занятия
        val sortedLessons = lessons.sortedWith(
            compareBy<LessonUi> { it.dayOfWeek }
                .thenBy { it.pairNumber }
        )
        
        // Данные
        sortedLessons.forEach { lesson ->
            val dayName = getDayName(lesson.dayOfWeek)
            val weekParity = when (lesson.weekParity.lowercase()) {
                "odd" -> "Нечётная"
                "even" -> "Чётная"
                "both" -> "Каждую неделю"
                else -> ""
            }
            
            val type = when (lesson.type.lowercase()) {
                "lecture" -> "Лекция"
                "practice" -> "Практика"
                "laboratory" -> "Лабораторная"
                else -> lesson.type
            }
            
            csv.appendLine(
                "${escapeCsvComma(dayName)}," +
                "${lesson.pairNumber}," +
                "${escapeCsvComma(lesson.time)}," +
                "${escapeCsvComma(lesson.subject)}," +
                "${escapeCsvComma(type)}," +
                "${escapeCsvComma(lesson.teacher)}," +
                "${escapeCsvComma(lesson.classroom)}," +
                "${escapeCsvComma(weekParity)}"
            )
        }
        
        return csv.toString()
    }
    
    /**
     * Получает название дня недели
     */
    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            else -> "Неизвестно"
        }
    }
    
    /**
     * Экранирует специальные символы для CSV (точка с запятой)
     */
    private fun escapeCsv(text: String): String {
        return if (text.contains(";") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
    
    /**
     * Экранирует специальные символы для CSV (запятая)
     */
    private fun escapeCsvComma(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
}

