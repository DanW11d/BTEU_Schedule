package com.example.bteu_schedule.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

/**
 * A4.9: Утилита для отслеживания направления скролла
 * 
 * Используется для умного скрытия навигации (вариант B):
 * - При скролле вниз → панель уезжает
 * - При скролле вверх → панель появляется
 */
@Composable
fun rememberScrollDirection(
    listState: LazyListState
): Boolean {
    var previousScrollOffset by remember { mutableStateOf(0) }
    var isScrollingDown by remember { mutableStateOf(false) }
    
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentOffset = listState.firstVisibleItemScrollOffset
        val currentItem = listState.firstVisibleItemIndex
        
        // Вычисляем общее смещение
        val totalOffset = currentItem * 1000 + currentOffset // Упрощённый расчёт
        
        // Определяем направление скролла
        isScrollingDown = totalOffset > previousScrollOffset
        
        previousScrollOffset = totalOffset
    }
    
    return isScrollingDown
}

/**
 * A4.9: Утилита для отслеживания направления скролла с порогом
 * 
 * Добавляет порог для предотвращения мерцания при небольших движениях
 */
@Composable
fun rememberScrollDirectionWithThreshold(
    listState: LazyListState,
    threshold: Int = 10 // Порог в пикселях
): Boolean {
    var previousScrollOffset by remember { mutableStateOf(0) }
    var isScrollingDown by remember { mutableStateOf(false) }
    
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentOffset = listState.firstVisibleItemScrollOffset
        val currentItem = listState.firstVisibleItemIndex
        
        // Вычисляем общее смещение
        val totalOffset = currentItem * 1000 + currentOffset
        
        // Определяем направление скролла только если изменение превышает порог
        val offsetDifference = totalOffset - previousScrollOffset
        if (kotlin.math.abs(offsetDifference) > threshold) {
            isScrollingDown = offsetDifference > 0
        }
        
        previousScrollOffset = totalOffset
    }
    
    return isScrollingDown
}

