package com.example.bteu_schedule.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

/**
 * Утилиты для оптимизации Compose компонентов
 * 
 * Предоставляет вспомогательные функции для:
 * - Кэширования дорогих вычислений
 * - Оптимизации recomposition
 * - Работы с производными состояниями
 */

/**
 * Кэширует результат дорогого вычисления с использованием remember
 * 
 * @param key Ключ для кэширования (результат будет пересчитан при изменении ключа)
 * @param computation Дорогое вычисление
 * @return Запомненный результат вычисления
 */
@Composable
inline fun <T> rememberExpensive(
    key: Any?,
    crossinline computation: () -> T
): T {
    return remember(key) {
        computation()
    }
}

/**
 * Кэширует результат дорогого вычисления с несколькими ключами
 * 
 * @param keys Ключи для кэширования (результат будет пересчитан при изменении любого ключа)
 * @param computation Дорогое вычисление
 * @return Запомненный результат вычисления
 */
@Composable
inline fun <T> rememberExpensive(
    vararg keys: Any?,
    crossinline computation: () -> T
): T {
    return remember(*keys) {
        computation()
    }
}

/**
 * Создает производное состояние, которое пересчитывается только при изменении зависимостей
 * 
 * Использование:
 * ```
 * val derivedValue by rememberDerivedState { expensiveComputation(state) }
 * ```
 */
@Composable
inline fun <T> rememberDerivedState(
    vararg keys: Any?,
    crossinline computation: () -> T
): T {
    return remember(*keys) {
        derivedStateOf { computation() }
    }.value
}

/**
 * Кэширует список, созданный из исходных данных
 * 
 * @param data Исходные данные
 * @param transform Функция преобразования
 * @return Кэшированный список
 */
@Composable
fun <T, R> rememberList(
    data: List<T>,
    transform: (T) -> R
): List<R> {
    return remember(data) {
        data.map(transform)
    }
}

/**
 * Кэширует Map, созданную из списка
 * 
 * @param data Исходный список
 * @param keySelector Функция выбора ключа
 * @param valueSelector Функция выбора значения (опционально)
 * @return Кэшированная Map
 */
@Composable
fun <T, K> rememberMap(
    data: List<T>,
    keySelector: (T) -> K
): Map<K, T> {
    return remember(data) {
        data.associateBy(keySelector)
    }
}

@Composable
fun <T, K, V> rememberMap(
    data: List<T>,
    keySelector: (T) -> K,
    valueSelector: (T) -> V
): Map<K, V> {
    return remember(data) {
        data.associateBy(keySelector, valueSelector)
    }
}

/**
 * Вспомогательная функция для создания стабильных ключей для списков
 * 
 * Рекомендуется использовать в LazyColumn с items() для оптимизации recomposition
 */
fun <T> T.stableKey(): Any = this ?: "null"

/**
 * Оптимизированная функция для фильтрации списков в Compose
 * 
 * @param list Исходный список
 * @param filter Функция фильтрации
 * @return Кэшированный отфильтрованный список
 */
@Composable
fun <T> rememberFiltered(
    list: List<T>,
    filter: (T) -> Boolean
): List<T> {
    return remember(list, filter) {
        list.filter(filter)
    }
}

/**
 * Оптимизированная функция для сортировки списков в Compose
 * 
 * @param list Исходный список
 * @param comparator Компаратор для сортировки
 * @return Кэшированный отсортированный список
 */
@Composable
fun <T> rememberSorted(
    list: List<T>,
    comparator: Comparator<T>
): List<T> {
    return remember(list, comparator) {
        list.sortedWith(comparator)
    }
}

