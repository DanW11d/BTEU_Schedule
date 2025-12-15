package com.example.bteu_schedule.ui.navigation

import androidx.compose.runtime.*

/**
 * A4.10: Стек навигации для каждого таба
 * 
 * На каждом табе приложение держит отдельный стек навигации.
 * Это позволяет сохранять состояние каждого таба при переключении между ними.
 */
data class TabNavigationStack(
    val destination: AppDestinations,
    val stack: List<AppDestinations> = listOf()
) {
    fun push(destination: AppDestinations): TabNavigationStack {
        return copy(stack = stack + destination)
    }
    
    fun pop(): TabNavigationStack {
        return if (stack.isNotEmpty()) {
            copy(stack = stack.dropLast(1))
        } else {
            this
        }
    }
    
    fun current(): AppDestinations {
        return stack.lastOrNull() ?: destination
    }
    
    fun canPop(): Boolean {
        return stack.isNotEmpty()
    }
}

/**
 * A4.10: Менеджер стеков навигации для всех табов
 */
@Composable
fun rememberTabNavigationStacks(): MutableState<Map<AppDestinations, TabNavigationStack>> {
    return remember {
        mutableStateOf(
            AppDestinations.entries.associateWith { destination ->
                TabNavigationStack(destination = destination)
            }
        )
    }
}

/**
 * A4.10: Получить стек навигации для конкретного таба
 */
@Composable
fun rememberTabStack(
    currentTab: AppDestinations,
    stacks: MutableState<Map<AppDestinations, TabNavigationStack>>
): TabNavigationStack {
    return remember(currentTab) {
        stacks.value[currentTab] ?: TabNavigationStack(currentTab)
    }
}

/**
 * A4.10: Обновить стек навигации для таба
 */
fun updateTabStack(
    tab: AppDestinations,
    stack: TabNavigationStack,
    stacks: MutableState<Map<AppDestinations, TabNavigationStack>>
) {
    stacks.value = stacks.value.toMutableMap().apply {
        put(tab, stack)
    }
}

