package com.example.bteu_schedule.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A4.1: Направления навигации в главном приложении
 * 
 * Структура навигации имеет 5 пунктов, каждый соответствует отдельному корневому экрану:
 * 1. Домой (Home) - главный экран с карточками разделов
 * 2. Поиск (Search) - поиск по расписанию, экзаменам, тестам
 * 3. Ассистент (Assistant) - AI помощник для вопросов
 * 4. Уведомления (Notifications) - список уведомлений
 * 5. Настройки (Settings) - настройки приложения
 * 
 * Преимущества:
 * - 5 пунктов = покрывает весь функционал
 * - Не создаёт перегруженности
 * - Пользователь сразу понимает, где что находится
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    HOME("Главная", Icons.Default.Home),
    SEARCH("Поиск", Icons.Default.Search),
    AI_CHAT("AI", Icons.Default.Psychology),
    NOTIFICATIONS("Уведомления", Icons.Default.Notifications),
    SETTINGS("Настройки", Icons.Default.Settings),
}

