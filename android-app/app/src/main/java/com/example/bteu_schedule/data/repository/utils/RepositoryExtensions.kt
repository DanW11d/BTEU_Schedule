package com.example.bteu_schedule.data.repository.utils

import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.utils.AppLogger
import com.example.bteu_schedule.utils.ErrorFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Extension функции для работы с Retrofit Response
 * Устраняют дублирование кода в репозиториях
 */

/**
 * Безопасно обрабатывает Retrofit Response и возвращает ApiResponse
 * 
 * @param tag Тег для логирования
 * @param transform Функция преобразования данных из DTO в Domain модель
 * @param operationName Название операции для логирования
 * @return ApiResponse с результатом операции
 */
suspend inline fun <T, R> Response<T>.toApiResponse(
    tag: String,
    operationName: String = "Операция",
    crossinline transform: (T) -> R
): ApiResponse<R> = withContext(Dispatchers.IO) {
    try {
        if (this@toApiResponse.isSuccessful && this@toApiResponse.body() != null) {
            val body = this@toApiResponse.body()!!
            val transformed = transform(body)
            
            AppLogger.d(tag, "$operationName успешно выполнена")
            ApiResponse.Success(transformed)
        } else {
            val errorMsg = ErrorFormatter.formatHttpError(
                this@toApiResponse.code(),
                this@toApiResponse.message()
            )
            AppLogger.e(tag, "Ошибка $operationName: HTTP ${this@toApiResponse.code()} - $errorMsg")
            ApiResponse.Error(errorMsg, this@toApiResponse.code())
        }
    } catch (e: Exception) {
        val errorMsg = ErrorFormatter.formatError(e)
        AppLogger.e(tag, "Ошибка при выполнении $operationName", e)
        ApiResponse.Error(errorMsg)
    }
}

/**
 * Упрощенная версия для операций без преобразования (например, для Unit)
 * 
 * @param tag Тег для логирования
 * @param operationName Название операции для логирования
 * @return ApiResponse с результатом операции
 */
suspend fun Response<*>.toApiResponse(
    tag: String,
    operationName: String = "Операция"
): ApiResponse<Unit> = withContext(Dispatchers.IO) {
    try {
        if (this@toApiResponse.isSuccessful) {
            AppLogger.d(tag, "$operationName успешно выполнена")
            ApiResponse.Success(Unit)
        } else {
            val errorMsg = ErrorFormatter.formatHttpError(
                this@toApiResponse.code(),
                this@toApiResponse.message()
            )
            AppLogger.e(tag, "Ошибка $operationName: HTTP ${this@toApiResponse.code()} - $errorMsg")
            ApiResponse.Error(errorMsg, this@toApiResponse.code())
        }
    } catch (e: Exception) {
        val errorMsg = ErrorFormatter.formatError(e)
        AppLogger.e(tag, "Ошибка при выполнении $operationName", e)
        ApiResponse.Error(errorMsg)
    }
}

/**
 * Обрабатывает список данных с преобразованием
 * 
 * @param tag Тег для логирования
 * @param operationName Название операции для логирования
 * @param transform Функция преобразования одного элемента списка
 * @return ApiResponse с преобразованным списком
 */
suspend inline fun <T, R> Response<List<T>>.toApiResponseList(
    tag: String,
    operationName: String = "Операция",
    crossinline transform: (T) -> R
): ApiResponse<List<R>> = toApiResponse(tag, operationName) { list ->
    list.map(transform)
}

/**
 * Выполняет операцию с обработкой ошибок и логированием
 * 
 * @param tag Тег для логирования
 * @param operationName Название операции для логирования
 * @param operation Операция для выполнения
 * @return ApiResponse с результатом
 */
suspend inline fun <T> executeWithErrorHandling(
    tag: String,
    operationName: String = "Операция",
    crossinline operation: suspend () -> T
): ApiResponse<T> = withContext(Dispatchers.IO) {
    try {
        val startTime = System.currentTimeMillis()
        AppLogger.d(tag, "Начало выполнения: $operationName")
        
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        
        AppLogger.d(tag, "$operationName успешно выполнена за ${duration}ms")
        ApiResponse.Success(result)
    } catch (e: Exception) {
        val errorMsg = ErrorFormatter.formatError(e)
        AppLogger.e(tag, "Ошибка при выполнении $operationName", e)
        ApiResponse.Error(errorMsg)
    }
}

/**
 * Выполняет операцию с детальным логированием для API запросов
 * 
 * @param tag Тег для логирования
 * @param operationName Название операции для логирования
 * @param operation Операция для выполнения (должна возвращать Response)
 * @param transform Функция преобразования данных
 * @return ApiResponse с результатом
 */
suspend inline fun <T, R> executeApiCall(
    tag: String,
    operationName: String,
    crossinline operation: suspend () -> Response<T>,
    crossinline transform: (T) -> R
): ApiResponse<R> = withContext(Dispatchers.IO) {
    try {
        AppLogger.d(tag, "═══════════════════════════════════════")
        AppLogger.d(tag, "НАЧАЛО ЗАПРОСА: $operationName")
        AppLogger.d(tag, "Запрос $operationName...")
        
        val startTime = System.currentTimeMillis()
        val response = operation()
        val endTime = System.currentTimeMillis()
        
        AppLogger.d(tag, "Ответ получен: код=${response.code()}, успех=${response.isSuccessful}, время=${endTime - startTime}ms")
        try {
            AppLogger.d(tag, "URL запроса: ${response.raw().request.url}")
        } catch (e: Exception) {
            AppLogger.d(tag, "Не удалось получить URL запроса: ${e.message}")
        }
        AppLogger.d(tag, "═══════════════════════════════════════")
        
        if (response.isSuccessful && response.body() != null) {
            val transformed = transform(response.body()!!)
            AppLogger.d(tag, "$operationName успешно выполнена")
            ApiResponse.Success(transformed)
        } else {
            val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
            AppLogger.e(tag, "Ошибка HTTP: ${response.code()} - $errorMsg")
            AppLogger.e(tag, "Тело ответа: ${response.errorBody()?.string()}")
            ApiResponse.Error(errorMsg, response.code())
        }
    } catch (e: Exception) {
        val errorMsg = ErrorFormatter.formatError(e)
        AppLogger.e(tag, "═══════════════════════════════════════")
        AppLogger.e(tag, "ОШИБКА ПОДКЛЮЧЕНИЯ К СЕРВЕРУ")
        AppLogger.e(tag, "Тип исключения: ${e.javaClass.name}")
        AppLogger.e(tag, "Сообщение: ${e.message}")
        AppLogger.e(tag, "Причина: ${e.cause?.message}")
        AppLogger.e(tag, "Stack trace:", e)
        AppLogger.e(tag, "═══════════════════════════════════════")
        ApiResponse.Error(errorMsg)
    }
}

/**
 * Безопасно получает тело ответа или возвращает ошибку
 */
fun <T> Response<T>.getBodyOrError(tag: String, operationName: String): ApiResponse<T> {
    return if (isSuccessful && body() != null) {
        ApiResponse.Success(body()!!)
    } else {
        val errorMsg = ErrorFormatter.formatHttpError(code(), message())
        AppLogger.e(tag, "Ошибка $operationName: HTTP ${code()} - $errorMsg")
        ApiResponse.Error(errorMsg, code())
    }
}

