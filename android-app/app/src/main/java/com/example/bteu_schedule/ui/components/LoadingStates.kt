package com.example.bteu_schedule.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.components.Skeleton

/**
 * A8. Состояния: загрузка, пусто, ошибка
 * 
 * 🎯 Цель:
 * - Пользователь всегда понимает, что происходит
 * - Нет «мертвых» экранов
 * - Даже при ошибках приложение выглядит аккуратно и дружелюбно
 */

/**
 * A8.1.1: Загрузка расписания дня / недели
 * 
 * Вместо пустого экрана + круговой индикатор:
 * - Несколько «заглушек» карточек (3–6 штук)
 * - Прямоугольники с радиусом 16–20dp
 * - Высота как у карточки пары (72–100dp)
 * - Фон: #E6ECF9 (светлая) / #1C2033 (тёмная)
 * - Лёгкий shimmer-анимация слева направо
 * - Заголовок: «Загружаем расписание…»
 * - Подзаголовок: «Это может занять несколько секунд»
 */
@Composable
fun ScheduleLoadingState(
    modifier: Modifier = Modifier,
    count: Int = 5 // A8.1.1: 3–6 штук (используем 5)
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp отступ
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // 16dp между элементами
    ) {
        // A8.1.1: Заголовок и подзаголовок над списком
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 8dp между заголовком и подзаголовком
        ) {
            // A8.4: Loading - спокойный текст
            Text(
                text = "Загружаем расписание…",
                style = MaterialTheme.typography.headlineSmall, // H2: 20sp SemiBold
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Это может занять несколько секунд",
                style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), // A8.4: спокойный текст (мягче)
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(DesignSpacing.M)) // 12dp отступ перед карточками
        
        // A8.1.1: Несколько «заглушек» карточек (3–6 штук)
        repeat(count) {
            LessonCardSkeleton(
                modifier = Modifier.fillMaxWidth()
            )
            if (it < count - 1) {
                Spacer(modifier = Modifier.height(DesignSpacing.Base)) // 16dp между карточками
            }
        }
    }
}

/**
 * A8.1.1: Skeleton карточки пары
 * 
 * Параметры:
 * - Радиус: 16–20dp (используем 20dp как у LessonCard)
 * - Высота: 72–100dp (используем 92dp как у LessonCard)
 * - Фон: #E6ECF9 (светлая) / #1C2033 (тёмная)
 * - Лёгкий shimmer-анимация слева направо
 */
@Composable
private fun LessonCardSkeleton(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // A8.1.1: Фон: #E6ECF9 (светлая) / #1C2033 (тёмная)
    val skeletonColor = if (isDarkTheme) {
        Color(0xFF1C2033) // A8.1.1: тёмная тема: #1C2033
    } else {
        Color(0xFFE6ECF9) // A8.1.1: светлая тема: #E6ECF9
    }
    
    // A8.1.1: Прямоугольники с радиусом 16–20dp (используем 20dp как у LessonCard)
    // Высота как у карточки пары (72–100dp, используем 92dp)
    Box(
        modifier = modifier
            .height(92.dp) // A8.1.1: Высота как у карточки пары (72–100dp)
            .clip(RoundedCornerShape(20.dp)) // A8.1.1: Радиус 16–20dp (используем 20dp)
            .background(skeletonColor)
    ) {
        // A8.1.1: Лёгкий shimmer-анимация слева направо
        Skeleton(
            modifier = Modifier.fillMaxSize(),
            height = 92.dp
        )
        
        // Внутренняя структура skeleton (имитация контента карточки)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSpacing.CardPaddingLarge), // 20dp padding
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
        ) {
            // Skeleton для иконки/бейджа
            Skeleton(
                width = 32.dp,
                height = 32.dp
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 8dp между строками
            ) {
                // Skeleton для названия предмета
                Skeleton(
                    height = 18.dp,
                    width = null
                )
                
                // Skeleton для преподавателя
                Skeleton(
                    height = 14.dp,
                    width = null
                )
            }
            
            // Skeleton для аудитории
            Skeleton(
                width = 60.dp,
                height = 14.dp
            )
        }
    }
}

/**
 * A8.1.2: Загрузка ассистента
 * 
 * В чате ассистента:
 * - Показываем «скелет» пузыря сообщения ассистента
 * - Индикатор «Ассистент печатает…» (3 точки)
 * - Создаёт ощущение живого диалога, как у мессенджеров
 */
@Composable
fun AssistantLoadingState(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSpacing.Base), // 16dp отступ
        horizontalArrangement = Arrangement.Start
    ) {
        // Skeleton пузыря сообщения ассистента
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(20.dp)) // Радиус как у пузыря сообщения
                .background(
                    color = if (isSystemInDarkTheme()) {
                        Color(0xFF1C2033) // A8.1.2: тёмная тема
                    } else {
                        Color(0xFFE6ECF9) // A8.1.2: светлая тема
                    }
                )
        ) {
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // Примерная высота пузыря
                height = 80.dp
            )
            
            // Внутренний контент skeleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSpacing.Base), // 16dp padding
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 8dp между строками
            ) {
                // Skeleton для текста сообщения (2-3 строки)
                repeat(3) {
                    Skeleton(
                        height = 14.dp,
                        width = null
                    )
                    if (it < 2) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(DesignSpacing.S)) // 8dp отступ
        
        // A8.1.2: Индикатор «Ассистент печатает…» (3 точки)
        // Используем существующий AnimatedDots из AiChatScreen
        // Но создадим отдельный компонент для переиспользования
        AnimatedTypingIndicator()
    }
}

/**
 * A8.1.2: Индикатор «Ассистент печатает…» (3 точки)
 * 
 * Анимация: три точки, по очереди changing opacity 0.3 → 1, duration 900ms, бесконечный цикл
 */
@Composable
private fun AnimatedTypingIndicator() {
    // A6.6: Анимация «Ассистент печатает…» - три точки, по очереди changing opacity 0.3 → 1, duration 900ms, бесконечный цикл
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    val dotColor = MaterialTheme.colorScheme.primary
    val dotSize = 8.dp
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.padding(DesignSpacing.Base) // 16dp padding
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot1Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot2Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot3Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

/**
 * A8.1.3: Загрузка списков (факультеты, группы, курсы)
 * 
 * Вместо пустоты — 5–7 заглушек карточек (ListCard).
 * Каждая — с серыми блоками вместо текста и иконки.
 */
@Composable
fun ListLoadingState(
    modifier: Modifier = Modifier,
    count: Int = 6 // A8.1.3: 5–7 заглушек (используем 6)
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(DesignSpacing.Base), // 16dp отступ
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // 16dp между карточками
    ) {
        items(count) {
            ListCardSkeleton()
        }
    }
}

/**
 * A8.1.3: Skeleton карточки списка (ListCard)
 * 
 * Параметры:
 * - Радиус: 20dp (как у ListCard)
 * - Высота: 72dp (как у ListCard)
 * - Фон: #E6ECF9 (светлая) / #1C2033 (тёмная)
 * - Серые блоки вместо текста и иконки
 */
@Composable
private fun ListCardSkeleton(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // A8.1.3: Фон: #E6ECF9 (светлая) / #1C2033 (тёмная)
    val skeletonColor = if (isDarkTheme) {
        Color(0xFF1C2033) // A8.1.3: тёмная тема: #1C2033
    } else {
        Color(0xFFE6ECF9) // A8.1.3: светлая тема: #E6ECF9
    }
    
    // A8.1.3: Радиус: 20dp, Высота: 72dp (как у ListCard)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp) // A8.1.3: Высота как у ListCard
            .clip(RoundedCornerShape(20.dp)) // A8.1.3: Радиус 20dp
            .background(skeletonColor)
    ) {
        // A8.1.3: Лёгкий shimmer-анимация слева направо
        Skeleton(
            modifier = Modifier.fillMaxSize(),
            height = 72.dp
        )
        
        // A8.1.3: Внутренняя структура skeleton (имитация ListCard)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSpacing.Base), // 16dp padding
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M), // 12dp между элементами
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // A8.1.3: Серый блок вместо иконки
            Skeleton(
                width = 24.dp,
                height = 24.dp
            )
            
            // A8.1.3: Серые блоки вместо текста
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // 8dp между строками
            ) {
                // Skeleton для заголовка
                Skeleton(
                    height = 16.dp,
                    width = null
                )
                
                // Skeleton для подзаголовка (опционально)
                Skeleton(
                    height = 14.dp,
                    width = 120.dp // Частичная ширина
                )
            }
            
            // A8.1.3: Серый блок вместо стрелки
            Skeleton(
                width = 24.dp,
                height = 24.dp
            )
        }
    }
}

/**
 * A8.1.4: Загрузка виджета
 * 
 * Если виджет ещё не успел получить данные:
 * - Показываем надпись: «Обновляем данные расписания…»
 * - Маленький shimmer-прогресс внизу
 */
@Composable
fun WidgetLoadingState(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpacing.Base), // 16dp padding
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // 12dp между элементами
    ) {
        // A8.1.4: Надпись: «Обновляем данные расписания…»
        Text(
            text = "Обновляем данные расписания…",
            style = MaterialTheme.typography.bodyMedium, // Body: 14sp Regular
            color = if (isDarkTheme) {
                Color(0xFFFFFFFF) // A7.9: #FFFFFF для тёмной темы
            } else {
                Color(0xFF0D1025) // Тёмный текст для светлой темы
            },
            textAlign = TextAlign.Center
        )
        
        // A8.1.4: Маленький shimmer-прогресс внизу
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp) // Маленькая высота для прогресса
                .clip(RoundedCornerShape(2.dp)) // Скругление для прогресса
        ) {
            Skeleton(
                modifier = Modifier.fillMaxSize(),
                height = 4.dp
            )
        }
    }
}

