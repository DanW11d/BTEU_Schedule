package com.example.bteu_schedule.data.remote.dto

/**
 * Базовый класс для API ответов
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

/**
 * Обертка для списков из API
 */
data class ListResponse<T>(
    val data: List<T>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

/**
 * Обертка для одиночных объектов из API
 */
data class SingleResponse<T>(
    val data: T
)

