package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.utils.AppLogger
import com.example.bteu_schedule.utils.ErrorFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Базовый класс для репозиториев
 * 
 * Предоставляет общую функциональность для всех репозиториев:
 * - Обработка ошибок
 * - Логирование
 * - Обработка Retrofit Response
 * 
 * Использование:
 * ```
 * class MyRepository : BaseRepository("MyRepository") {
 *     suspend fun getData(): ApiResponse<List<Data>> {
 *         return executeApiCall(
 *             operationName = "Получение данных",
 *             operation = { api.getData() },
 *             transform = { it.map { dto -> dto.toDomain() } }
 *         )
 *     }
 * }
 * ```
 */
abstract class BaseRepository(
    protected val repositoryTag: String
) {
    
    /**
     * Выполняет API запрос с обработкой ошибок
     * 
     * @param operationName Название операции для логирования
     * @param operation Функция выполнения API запроса
     * @param transform Функция преобразования DTO в Domain модель
     * @return ApiResponse с результатом
     */
    protected suspend inline fun <T, R> executeApiCall(
        operationName: String,
        crossinline operation: suspend () -> Response<T>,
        crossinline transform: (T) -> R
    ): ApiResponse<R> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            AppLogger.d(repositoryTag, "Начало: $operationName")
            
            val response = operation()
            val duration = System.currentTimeMillis() - startTime
            
            AppLogger.d(repositoryTag, "Ответ получен: код=${response.code()}, успех=${response.isSuccessful}, время=${duration}ms")
            
            if (response.isSuccessful && response.body() != null) {
                val transformed = transform(response.body()!!)
                AppLogger.d(repositoryTag, "$operationName успешно выполнена")
                ApiResponse.Success(transformed)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e(repositoryTag, "Ошибка $operationName: HTTP ${response.code()} - $errorMsg")
                ApiResponse.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e(repositoryTag, "Ошибка при выполнении $operationName", e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Выполняет API запрос для списков с преобразованием
     * 
     * @param operationName Название операции
     * @param operation Функция выполнения API запроса
     * @param transform Функция преобразования одного элемента списка
     * @return ApiResponse с преобразованным списком
     */
    protected suspend inline fun <T, R> executeApiCallList(
        operationName: String,
        crossinline operation: suspend () -> Response<List<T>>,
        crossinline transform: (T) -> R
    ): ApiResponse<List<R>> = executeApiCall(operationName, operation) { list ->
        list.map(transform)
    }
    
    /**
     * Выполняет API запрос без преобразования (для Unit операций)
     * 
     * @param operationName Название операции
     * @param operation Функция выполнения API запроса
     * @return ApiResponse<Unit>
     */
    protected suspend inline fun executeApiCallUnit(
        operationName: String,
        crossinline operation: suspend () -> Response<*>
    ): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = operation()
            
            if (response.isSuccessful) {
                AppLogger.d(repositoryTag, "$operationName успешно выполнена")
                ApiResponse.Success(Unit)
            } else {
                val errorMsg = ErrorFormatter.formatHttpError(response.code(), response.message())
                AppLogger.e(repositoryTag, "Ошибка $operationName: HTTP ${response.code()} - $errorMsg")
                ApiResponse.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e(repositoryTag, "Ошибка при выполнении $operationName", e)
            ApiResponse.Error(errorMsg)
        }
    }
    
    /**
     * Выполняет произвольную операцию с обработкой ошибок
     * 
     * @param operationName Название операции
     * @param operation Функция выполнения операции
     * @return ApiResponse с результатом
     */
    protected suspend inline fun <T> executeOperation(
        operationName: String,
        crossinline operation: suspend () -> T
    ): ApiResponse<T> = withContext(Dispatchers.IO) {
        try {
            val result = operation()
            AppLogger.d(repositoryTag, "$operationName успешно выполнена")
            ApiResponse.Success(result)
        } catch (e: Exception) {
            val errorMsg = ErrorFormatter.formatError(e)
            AppLogger.e(repositoryTag, "Ошибка при выполнении $operationName", e)
            ApiResponse.Error(errorMsg)
        }
    }
}

