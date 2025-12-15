package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing

/**
 * A8. Состояния: загрузка, пусто, ошибка
 * 
 * A8.2. Empty — нет данных / пусто
 * 
 * Empty ≠ ошибка.
 * Empty значит: всё работает, но по данным реально пусто.
 */

/**
 * A8.2.1: Пустое расписание
 * 
 * Если на день нет ни одной пары:
 * - Красивая карточка-пустышка
 * - Иконка/мини-иллюстрация (книга закрыта, календарь с галочкой и т.п.)
 * - Заголовок: «Сегодня у тебя нет занятий 🎉»
 * - Подзаголовок (опционально): «Можно отдохнуть или заняться своими делами.»
 */
@Composable
fun EmptyScheduleState(
    modifier: Modifier = Modifier,
    title: String = "Сегодня у тебя нет занятий 🎉",
    subtitle: String? = "Можно отдохнуть или заняться своими делами.",
    icon: ImageVector = Icons.Default.EventAvailable
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // A8.2.1: Красивая карточка-пустышка
    // A8.4: Empty - светлый фон, минимум визуального шума
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignRadius.L)) // 24dp радиус
            .background(
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface // A8.4: светлый фон
                }
            )
            .padding(DesignSpacing.CardPaddingLarge) // 20dp padding
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
        ) {
            // A8.2.1: Иконка/мини-иллюстрация
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(DesignRadius.M)) // 16dp радиус
                    .background(
                        color = if (isDarkTheme) {
                            Color(0xFF1C2033) // Тёмная тема
                        } else {
                            Color(0xFFE6ECF9) // Светлая тема
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // A8.2.1: Заголовок
            // A8.4: Empty - крупный текст
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium, // A8.4: H2: 20sp SemiBold (крупнее)
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // A8.2.1: Подзаголовок (опционально)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A8.2.2: Нет экзаменов / зачётов
 * 
 * Карточка:
 * - «Ближайшие экзамены не найдены 🎓»
 * - «Похоже, сейчас у тебя нет запланированных аттестаций.»
 */
@Composable
fun EmptyExamsState(
    modifier: Modifier = Modifier,
    title: String = "Ближайшие экзамены не найдены 🎓",
    subtitle: String = "Похоже, сейчас у тебя нет запланированных аттестаций.",
    icon: ImageVector = Icons.Default.School
) {
    EmptyScheduleState(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon
    )
}

/**
 * A8.2.3: Пустой список (факультетов/групп по фильтру)
 * 
 * Сообщение:
 * - «По выбранным фильтрам ничего не найдено.»
 * - Кнопка: «Сбросить фильтры»
 */
@Composable
fun EmptyFilteredListState(
    modifier: Modifier = Modifier,
    title: String = "По выбранным фильтрам ничего не найдено.",
    buttonText: String = "Сбросить фильтры",
    onResetFilters: () -> Unit,
    icon: ImageVector = Icons.Default.FilterList
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp отступ
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // 16dp между элементами
    ) {
        // A8.2.3: Иконка
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(DesignRadius.M)) // 16dp радиус
                .background(
                    color = if (isDarkTheme) {
                        Color(0xFF1C2033) // Тёмная тема
                    } else {
                        Color(0xFFE6ECF9) // Светлая тема
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // A8.2.3: Заголовок
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall, // H2: 20sp SemiBold
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // A8.2.3: Кнопка «Сбросить фильтры»
        AppButton(
            text = buttonText,
            onClick = onResetFilters,
            variant = AppButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A8.2.4: Ассистент, если нет истории
 * 
 * Первый запуск чата:
 * - Пузырь ассистента:
 * - «Привет! Я помогу тебе с расписанием 💙
 * - Можешь спросить: "Какие пары завтра?" или "Когда следующий экзамен?"»
 */
@Composable
fun EmptyAssistantState(
    modifier: Modifier = Modifier,
    message: String = "Привет! Я помогу тебе с расписанием 💙\n\nМожешь спросить: \"Какие пары завтра?\" или \"Когда следующий экзамен?\"",
    onSuggestionClick: ((String) -> Unit)? = null
) {
    // Используем существующий AssistantCard для отображения пустого состояния
    AssistantCard(
        modifier = modifier,
        avatar = Icons.Default.SmartToy, // Аватар ассистента
        message = message,
        timestamp = null,
        suggestions = listOf(
            "Какие пары завтра?",
            "Когда следующий экзамен?",
            "Покажи расписание на неделю"
        ),
        onSuggestionClick = onSuggestionClick
    )
}

/**
 * A8.2.5: Виджет, если нет группы
 * 
 * Если пользователь не выбрал группу в приложении:
 * - «Выбери группу в приложении, чтобы я подсказал расписание.»
 * - Кнопка: «Открыть приложение»
 */
@Composable
fun EmptyWidgetGroupState(
    modifier: Modifier = Modifier,
    title: String = "Выбери группу в приложении, чтобы я подсказал расписание.",
    buttonText: String = "Открыть приложение",
    onOpenApp: () -> Unit,
    icon: ImageVector = Icons.Default.Group
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
    ) {
        // A8.2.5: Иконка
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(DesignRadius.M)) // 16dp радиус
                .background(
                    color = if (isDarkTheme) {
                        Color(0xFF1C2033) // Тёмная тема
                    } else {
                        Color(0xFFE6ECF9) // Светлая тема
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isDarkTheme) {
                    Color(0xFF3A4DFF) // A7.9: #3A4DFF для тёмной темы
                } else {
                    Color(0xFF4C6CFF) // #4C6CFF для светлой темы
                }
            )
        }
        
        // A8.2.5: Заголовок
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
            color = if (isDarkTheme) {
                Color(0xFFFFFFFF) // A7.9: #FFFFFF для тёмной темы
            } else {
                Color(0xFF0D1025) // Тёмный текст для светлой темы
            },
            textAlign = TextAlign.Center
        )
        
        // A8.2.5: Кнопка «Открыть приложение»
        AppButton(
            text = buttonText,
            onClick = onOpenApp,
            variant = AppButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

