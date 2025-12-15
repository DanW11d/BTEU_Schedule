package com.example.bteu_schedule.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Предопределенные градиенты для приложения
 * Обновлено на новую синюю цветовую систему
 */
object AppGradients {
    
    // Градиенты для основных экранов (обновлены на синие)
    val Schedule = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val Exams = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    val Tests = listOf(Color(0xFF7A8BFF), Color(0xFF000064)) // Soft Blue
    val Primary = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val Welcome = listOf(Color(0xFF3A4DFF), Color(0xFF4C6CFF), Color(0xFF000064)) // Brand gradient
    
    // Градиенты для типов занятий (обновлены на синие)
    val Lecture = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val Practice = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    val Laboratory = listOf(Color(0xFF7A8BFF), Color(0xFF000064)) // Soft Blue
    
    // Градиенты для карточек главного экрана (обновлены на синие)
    val ScheduleCard = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val ExamsCard = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    val TestsCard = listOf(Color(0xFF7A8BFF), Color(0xFF000064)) // Soft Blue
    val BellScheduleCard = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val DepartmentsCard = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    
    // Градиенты для других элементов (обновлены на синие)
    val Settings = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val Notifications = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    val Search = listOf(Color(0xFF3A4DFF), Color(0xFF000064)) // Classic Blue
    val Profile = listOf(Color(0xFF4C6CFF), Color(0xFF000064)) // Bright Blue
    
    /**
     * Создает вертикальный градиент
     */
    fun verticalGradient(colors: List<Color>): Brush {
        return Brush.verticalGradient(colors = colors)
    }
    
    /**
     * Создает горизонтальный градиент
     */
    fun horizontalGradient(colors: List<Color>): Brush {
        return Brush.horizontalGradient(colors = colors)
    }
    
    /**
     * Создает линейный градиент
     */
    fun linearGradient(colors: List<Color>): Brush {
        return Brush.linearGradient(colors = colors)
    }
}
