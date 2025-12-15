package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.ui.theme.AppGradients
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.MotionEasing
import com.example.bteu_schedule.ui.theme.MotionDuration
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.isAnimationEnabled

/**
 * Получить градиент для типа занятия
 * 
 * Оптимизированная версия с кэшированием нормализованного типа
 */
private fun getGradientForLessonType(type: String): List<Color> {
    val normalizedType = type.lowercase().trim()
    return when (normalizedType) {
        "лекция", "lecture", "л", "ст." -> AppGradients.Lecture
        "практика", "practice", "п", "практ", "пр." -> AppGradients.Practice
        "лабораторная", "lab", "лб", "лабораторная работа", "лаб." -> AppGradients.Laboratory
        else -> AppGradients.Lecture // По умолчанию - лекция
    }
}

/**
 * Получить сокращение типа занятия для отображения
 * 
 * Оптимизированная версия с кэшированием нормализованного типа
 */
private fun getLessonTypeAbbreviation(type: String): String {
    val normalizedType = type.lowercase().trim()
    return when (normalizedType) {
        "лекция", "lecture", "л", "ст." -> "ст."
        "практика", "practice", "п", "практ", "пр." -> "пр."
        "лабораторная", "lab", "лб", "лабораторная работа", "лаб." -> "лаб."
        else -> type
    }
}

/**
 * Lesson Card (Карточка пары) — UI-KIT компонент
 * 
 * Свойства:
 * - pairNumber: Int
 * - timeRange: String
 * - subject: String
 * - teacher: String
 * - auditorium: String
 * - weekType: String?
 * - subgroup: String?
 * 
 * Стиль:
 * - Радиус: 20dp
 * - Контрастный бейдж (по типу пары)
 * - Возможность подсветить текущую пару
 */
@Composable
fun LessonCard(
    pairNumber: Int,
    timeRange: String,
    subject: String,
    teacher: String,
    auditorium: String,
    weekType: String? = null,
    subgroup: String? = null,
    lessonType: String = "лекция", // Для определения градиента и бейджа
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    isCurrent: Boolean = false, // Подсветка текущей пары
    enabled: Boolean = true // A3.7: Disabled состояние
) {
    // Оптимизация: кэшируем дорогие вычисления градиента и сокращения типа
    val gradientColors = remember(lessonType) {
        getGradientForLessonType(lessonType)
    }
    val typeAbbreviation = remember(lessonType) {
        getLessonTypeAbbreviation(lessonType)
    }
    
    // Премиальная анимация нажатия
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hapticFeedback = rememberHapticFeedback() // A3.8: Виброотклик
    
    // A3.8: Виброотклик при нажатии (Impact Light для всех кликабельных элементов)
    LaunchedEffect(isPressed) {
        if (isPressed && enabled && onTap != null) {
            hapticFeedback.performLightImpact()
        }
    }
    
    // A6.3.3: При нажатии — scale 0.97
    // A3.7: Normal - scale 1, Pressed - scale 0.97, Disabled - альфа 50%
    // A3.10: Учитываем настройки доступности анимаций
    val animationEnabled = isAnimationEnabled()
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f // Disabled - без масштабирования
            isPressed -> 0.97f // A6.3.3: scale 0.97 при нажатии
            else -> 1f
        },
        animationSpec = if (animationEnabled) {
            tween(
                durationMillis = 150,
                easing = FastOutSlowInEasing
            )
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "cardPress"
    )
    
    // A3.7: Disabled - альфа 50%
    // A3.10: Альфа всегда анимируется (fade разрешен)
    val alpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.5f // A3.7: альфа 50% для disabled
            isPressed -> 0.9f
            else -> 1f
        },
        animationSpec = if (animationEnabled) {
            tween(
                durationMillis = 150,
                easing = FastOutSlowInEasing
            )
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "cardAlpha"
    )
    
    // A3.7: Normal - тень 8dp, Pressed - тень 12dp
    // A3.10: Elevation всегда анимируется (fade разрешен)
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 12f // A3.7: 12dp при pressed
            else -> 8f // A3.7: 8dp normal
        },
        animationSpec = if (animationEnabled) {
            tween(
                durationMillis = 150,
                easing = FastOutSlowInEasing
            )
        } else {
            // A3.10: Мгновенное изменение без анимации, если анимации отключены
            androidx.compose.animation.core.snap<Float>()
        },
        label = "cardElevation"
    )
    
    // A6.3.3: Подсветка текущей пары — мягкий fade 200ms
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(
            durationMillis = 200, // A6.3.3: Мягкий fade 200ms
            easing = MotionEasing.EaseOutCubic
        ),
        label = "currentPairBackground"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onTap != null && enabled) {
                    Modifier
                        .focusable() // A3.7: Поддержка Focused состояния
                        .clickable(
                            onClick = onTap,
                            interactionSource = interactionSource,
                            indication = null
                        )
                } else {
                    Modifier
                }
            )
            .scale(scale)
            .alpha(alpha), // A3.7: альфа 50% для disabled
        shape = RoundedCornerShape(DesignRadius.ListCard), // 20dp радиус
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor) // A6.3.3: Подсветка текущей пары с fade
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = AppGradients.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(DesignRadius.M) // 16dp радиус
                )
                .padding(DesignSpacing.CardPadding) // 16dp паддинг
        ) {
            // Номер пары
            Column(
                modifier = Modifier.width(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pairNumber.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(DesignSpacing.Base)) // 16dp
            
            HorizontalDivider(
                modifier = Modifier
                    .height(64.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.width(DesignSpacing.Base)) // 16dp
            
            // Основная информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Название предмета и бейдж типа пары
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S) // 8dp
                ) {
                    // A3.6: Текст переносится при увеличении шрифта
                    Text(
                        text = subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 2, // Максимум 2 строки при увеличении шрифта
                        overflow = TextOverflow.Ellipsis
                    )
                    // Контрастный бейдж типа пары
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.3f), // Более контрастный
                                shape = RoundedCornerShape(DesignRadius.XS) // 8dp
                            )
                            .padding(horizontal = DesignSpacing.S, vertical = DesignSpacing.XS) // 8dp горизонтально, 4dp вертикально
                    ) {
                        Text(
                            text = typeAbbreviation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold // Более контрастный
                        )
                    }
                    // A6.3.3: Бейдж подгруппы выезжает
                    AnimatedVisibility(
                        visible = subgroup != null,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = MotionDuration.Medium,
                                easing = MotionEasing.EaseOutCubic
                            )
                        ) + slideInHorizontally(
                            initialOffsetX = { -it / 4 }, // A6.3.3: Бейдж выезжает слева
                            animationSpec = tween(
                                durationMillis = MotionDuration.Medium,
                                easing = MotionEasing.EaseOutCubic
                            )
                        )
                    ) {
                        if (subgroup != null) {
                            Spacer(modifier = Modifier.width(DesignSpacing.XS)) // 4dp
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(DesignRadius.XS)
                                    )
                                    .padding(horizontal = DesignSpacing.S, vertical = DesignSpacing.XS)
                            ) {
                                Text(
                                    text = subgroup,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1, // Dynamic Type: ограничение для бейджа
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    // Бейдж типа недели (если указан)
                    if (weekType != null) {
                        Spacer(modifier = Modifier.width(DesignSpacing.XS)) // 4dp
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(DesignRadius.XS)
                                )
                                .padding(horizontal = DesignSpacing.S, vertical = DesignSpacing.XS)
                        ) {
                            Text(
                                text = weekType,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, // Dynamic Type: ограничение для бейджа
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignSpacing.M)) // 12dp
                
                // Преподаватель
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S) // 8dp
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Преподаватель",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    // A3.2: Secondary текст на градиенте - белый с минимум 80% яркости (0.9f >= 0.8f ✅)
                    // A3.6: Текст переносится при увеличении шрифта
                    Text(
                        text = teacher,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f), // Минимум 80% для Secondary текста
                        maxLines = 2, // Максимум 2 строки при увеличении шрифта
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSpacing.S)) // 8dp
                
                // Аудитория и время
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S) // 8dp
                ) {
                    Icon(
                        Icons.Default.Room,
                        contentDescription = "Аудитория",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    // A3.2: Secondary текст на градиенте - белый с минимум 80% яркости (0.9f >= 0.8f ✅)
                    // A3.6: Текст переносится при увеличении шрифта
                    Text(
                        text = auditorium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f), // Минимум 80% для Secondary текста
                        maxLines = 1, // Одна строка для аудитории
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(DesignSpacing.Base)) // 16dp
                    
                    // Время
                    Text(
                        text = "🕐 $timeRange",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1, // Dynamic Type: ограничение для времени
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Перегрузка для обратной совместимости
 * Использует LessonUi модель
 */
@Composable
fun LessonCard(
    lesson: LessonUi,
    displayPairNumber: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isCurrent: Boolean = false
) {
    LessonCard(
        pairNumber = displayPairNumber,
        timeRange = lesson.time,
        subject = lesson.subject,
        teacher = lesson.teacher,
        auditorium = lesson.classroom,
        weekType = when (lesson.weekParity) {
            "odd" -> "Нечётная"
            "even" -> "Чётная"
            else -> null
        },
        subgroup = null, // Можно добавить в LessonUi при необходимости
        lessonType = lesson.type,
        modifier = modifier,
        onTap = onClick,
        isCurrent = isCurrent
    )
}
