package com.example.bteu_schedule.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

/**
 * A8. Состояния: загрузка, пусто, ошибка
 * 
 * A8.5. Логика перехода между состояниями
 * 
 * Важно: не прыгать в разные состояния слишком резко.
 * - Смену между Loading → Content / Empty / Error можно делать через fade 150–200 ms.
 */

/**
 * A8.5: Плавный переход между состояниями
 * 
 * Использует fade анимацию 150–200ms для плавной смены состояний.
 * 
 * Пример для расписания:
 * - Начали загрузку → показываем Loading (skeleton)
 * - Данные пришли → если есть пары → показываем список, если нет → показываем Empty
 * - Если загрузка упала → показываем Error
 * - При нажатии «Повторить» → снова Loading → попытка
 */
@Composable
fun <T> StateTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            // A8.5: Fade анимация 150–200ms для плавной смены состояний
            fadeIn(
                animationSpec = tween(durationMillis = 180) // 180ms (в диапазоне 150–200ms)
            ) togetherWith fadeOut(
                animationSpec = tween(durationMillis = 150) // 150ms для быстрого исчезновения
            )
        },
        label = "state_transition"
    ) { state ->
        content(state)
    }
}

/**
 * A8.5: Переход между состояниями с ключом для стабильности
 * 
 * Используется когда нужно гарантировать пересоздание контента при смене состояния.
 */
@Composable
fun <T> StateTransitionWithKey(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            // A8.5: Fade анимация 150–200ms
            fadeIn(
                animationSpec = tween(durationMillis = 180)
            ) togetherWith fadeOut(
                animationSpec = tween(durationMillis = 150)
            )
        },
        label = "state_transition_with_key"
    ) { state ->
        // Используем key для гарантии пересоздания контента
        key(state) {
            content(state)
        }
    }
}

