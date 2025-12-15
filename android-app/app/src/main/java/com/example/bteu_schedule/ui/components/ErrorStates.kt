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
 * A8.3. Error — ошибки сети/сервера/данных
 * 
 * Нельзя просто показывать "Ошибка" и код.
 * Нужно:
 * - Объяснить человеческим языком
 * - Предложить действие
 * - Не пугать
 * 
 * Общие принципы:
 * - Иконка ⚠
 * - Короткое объяснение
 * - Кнопка для повтора / исправления
 */

/**
 * A8.3.1: Ошибка загрузки расписания
 * 
 * Карточка на фоне:
 * - Заголовок: «Не удалось загрузить расписание 😕»
 * - Подзаголовок: «Проверь интернет или попробуй обновить ещё раз.»
 * - Кнопка: «Повторить попытку»
 */
@Composable
fun ScheduleErrorState(
    modifier: Modifier = Modifier,
    title: String = "Не удалось загрузить расписание 😕",
    subtitle: String? = "Проверь интернет или попробуй обновить ещё раз.",
    buttonText: String = "Повторить попытку",
    onRetry: () -> Unit,
    icon: ImageVector = Icons.Default.Warning
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // A8.3.1: Карточка на фоне
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignRadius.L)) // 24dp радиус
            .background(
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(DesignSpacing.CardPaddingLarge) // 20dp padding
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
        ) {
            // A8.3.1: Иконка ⚠
            // A8.4: Error - иконка тревоги ⚠, но без красной заливки на весь экран
            // Красный используем только точечно (иконка/акцент)
            Icon(
                imageVector = icon,
                contentDescription = "Ошибка",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error // A8.4: красный только для иконки
            )
            
            // A8.3.1: Заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall, // H2: 20sp SemiBold
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // A8.3.1: Подзаголовок (опционально)
            if (subtitle != null && subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // A8.3.1: Кнопка «Повторить попытку»
            AppButton(
                text = buttonText,
                onClick = onRetry,
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * A8.3.1: Баннер для кэшированных данных
 * 
 * Если есть кэш старых данных — показываем старое расписание + баннер:
 * «Показываем последние сохранённые данные.
 * Обновить сейчас?»
 */
@Composable
fun CachedDataBanner(
    modifier: Modifier = Modifier,
    title: String = "Показываем последние сохранённые данные.",
    buttonText: String = "Обновить сейчас?",
    onRefresh: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // A8.3.1: Баннер на фоне расписания
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignRadius.M)) // 16dp радиус
            .background(
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                }
            )
            .padding(DesignSpacing.Base) // 16dp padding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A8.3.1: Текст
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            // A8.3.1: Кнопка «Обновить сейчас?»
            AppButton(
                text = buttonText,
                onClick = onRefresh,
                variant = AppButtonVariant.Secondary,
                modifier = Modifier.padding(start = DesignSpacing.M) // 12dp отступ
            )
        }
    }
}

/**
 * A8.3.2: Ошибка сервера / API
 * 
 * Сообщение:
 * - «Сервер расписания сейчас недоступен.
 * - Это может быть временно, попробуй позже.»
 * - Кнопка: «Попробовать снова»
 */
@Composable
fun ServerErrorState(
    modifier: Modifier = Modifier,
    title: String = "Сервер расписания сейчас недоступен.",
    subtitle: String = "Это может быть временно, попробуй позже.",
    buttonText: String = "Попробовать снова",
    onRetry: () -> Unit,
    icon: ImageVector = Icons.Default.CloudOff
) {
    ScheduleErrorState(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        buttonText = buttonText,
        onRetry = onRetry,
        icon = icon
    )
}

/**
 * A8.3.3: Ошибка ассистента
 * 
 * Если ассистент не смог получить ответ (API/сеть):
 * - В чате показываем сообщение ассистента:
 * - «Я сейчас не могу получить ответ от сервера 😔
 * - Попробуй ещё раз через пару минут.»
 * - Кнопка: «Попробовать снова» (повтор того же запроса)
 */
@Composable
fun AssistantErrorState(
    modifier: Modifier = Modifier,
    message: String = "Я сейчас не могу получить ответ от сервера 😔\n\nПопробуй ещё раз через пару минут.",
    buttonText: String = "Попробовать снова",
    onRetry: () -> Unit
) {
    // Используем AssistantCard для отображения ошибки в стиле чата
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
    ) {
        // A8.3.3: Сообщение ассистента с ошибкой
        AssistantCard(
            avatar = Icons.Default.SmartToy,
            message = message,
            timestamp = null,
            suggestions = null,
            onSuggestionClick = null
        )
        
        // A8.3.3: Кнопка «Попробовать снова»
        AppButton(
            text = buttonText,
            onClick = onRetry,
            variant = AppButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A8.3.4: Ошибка виджета
 * 
 * Мини-карта:
 * - «Не удалось обновить данные расписания.»
 * - Кнопка: «Открыть приложение»
 */
@Composable
fun WidgetErrorState(
    modifier: Modifier = Modifier,
    title: String = "Не удалось обновить данные расписания.",
    buttonText: String = "Открыть приложение",
    onOpenApp: () -> Unit,
    icon: ImageVector = Icons.Default.Warning
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
    ) {
        // A8.3.4: Иконка ⚠
        Icon(
            imageVector = icon,
            contentDescription = "Ошибка",
            modifier = Modifier.size(32.dp),
            tint = if (isDarkTheme) {
                Color(0xFFFF6B6B) // Красный для тёмной темы
            } else {
                MaterialTheme.colorScheme.error
            }
        )
        
        // A8.3.4: Заголовок
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
        
        // A8.3.4: Кнопка «Открыть приложение»
        AppButton(
            text = buttonText,
            onClick = onOpenApp,
            variant = AppButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A8.3.5: Техничные ошибки (DBF сломался, формат не тот)
 * 
 * Это не для пользователя, а для логов/разработки:
 * - в UI показываем:
 * - «Произошла внутренняя ошибка обработки расписания.
 * - Разработчик уже может посмотреть логи.»
 * - (или вообще нейтрально: «Не удалось обновить расписание.»)
 */
@Composable
fun TechnicalErrorState(
    modifier: Modifier = Modifier,
    title: String = "Произошла внутренняя ошибка обработки расписания.",
    subtitle: String = "Разработчик уже может посмотреть логи.",
    buttonText: String = "Повторить попытку",
    onRetry: () -> Unit,
    icon: ImageVector = Icons.Default.BugReport
) {
    ScheduleErrorState(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        buttonText = buttonText,
        onRetry = onRetry,
        icon = icon
    )
}

/**
 * A8.3.5: Нейтральная ошибка (для пользователя)
 * 
 * Альтернативный вариант для техничных ошибок:
 * - «Не удалось обновить расписание.»
 */
@Composable
fun NeutralErrorState(
    modifier: Modifier = Modifier,
    title: String = "Не удалось обновить расписание.",
    buttonText: String = "Повторить попытку",
    onRetry: () -> Unit,
    icon: ImageVector = Icons.Default.Warning
) {
    ScheduleErrorState(
        modifier = modifier,
        title = title,
        subtitle = "", // Пустая строка для нейтральности
        buttonText = buttonText,
        onRetry = onRetry,
        icon = icon
    )
}

