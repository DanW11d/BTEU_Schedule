package com.example.bteu_schedule.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Утилита для тестирования адаптивности к Dynamic Type
 * 
 * Использование:
 * 1. Включите в настройках Android: Settings > Display > Font size
 * 2. Установите размер шрифта на "Large" или "Largest"
 * 3. Проверьте все экраны приложения на обрезание текста
 * 
 * Рекомендуемые размеры для тестирования:
 * - Small: 0.85x
 * - Default: 1.0x
 * - Large: 1.15x
 * - Largest: 1.3x
 * - Extra Large: 1.5x (если доступно)
 */

/**
 * Получить текущий масштаб шрифта системы
 */
@Composable
fun getSystemFontScale(): Float {
    val configuration = LocalConfiguration.current
    return configuration.fontScale
}

/**
 * Тестовый компонент для проверки адаптивности текста
 * 
 * Показывает примеры текста с разными стилями для проверки обрезания
 */
@Composable
fun DynamicTypeTestCard(
    modifier: Modifier = Modifier
) {
    val fontScale = getSystemFontScale()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Font Scale: ${String.format("%.2f", fontScale)}x",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Headline Large (24sp)",
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Text(
                text = "Headline Medium (20sp)",
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Text(
                text = "Headline Small (18sp)",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Text(
                text = "Body Large (16sp) - Основной текст приложения",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Text(
                text = "Body Medium (13sp) - Вспомогательный текст",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Чеклист для тестирования Dynamic Type:
 * 
 * 1. Проверить все экраны с текстом:
 *    - HomeScreen (карточки разделов)
 *    - ScheduleScreen (карточки пар)
 *    - ExamsScreen (карточки экзаменов)
 *    - TestsScreen (карточки зачетов)
 *    - NotificationsScreen (карточки уведомлений)
 *    - SettingsScreen (пункты меню)
 *    - AboutScreen (информация)
 * 
 * 2. Проверить компоненты:
 *    - ModernGradientCard (заголовки и подзаголовки)
 *    - ListCard (названия и описания)
 *    - PrimaryCard (заголовки)
 *    - LessonCard (названия предметов, преподаватели)
 *    - AdaptiveHeader (заголовки экранов)
 * 
 * 3. Проверить на разных размерах шрифта:
 *    - Small (0.85x)
 *    - Default (1.0x)
 *    - Large (1.15x)
 *    - Largest (1.3x)
 * 
 * 4. Что проверить:
 *    - Текст не обрезается сверху/снизу
 *    - Текст переносится на новую строку (если maxLines > 1)
 *    - Многострочный текст показывает ellipsis при превышении maxLines
 *    - Контейнеры расширяются для размещения текста
 *    - Нет фиксированных высот, которые обрезают текст
 *    - Line-height достаточен для предотвращения обрезания
 */

