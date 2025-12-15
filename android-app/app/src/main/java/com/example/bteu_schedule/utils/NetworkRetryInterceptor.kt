package com.example.bteu_schedule.utils

import com.example.bteu_schedule.data.config.AppConstants
import com.example.bteu_schedule.utils.AppLogger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Интерцептор для автоматической повторной попытки сетевых запросов при ошибках
 * 
 * Использует экспоненциальную задержку между попытками
 */

class NetworkRetryInterceptor(
    private val maxRetries: Int = AppConstants.NETWORK_MAX_RETRIES,
    private val initialDelayMs: Long = AppConstants.NETWORK_INITIAL_RETRY_DELAY_MS,
    private val maxDelayMs: Long = AppConstants.NETWORK_MAX_RETRY_DELAY_MS
) : Interceptor {

    companion object {
        private const val TAG = "NetworkRetryInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null

        // Не повторяем запросы для некоторых методов
        if (shouldSkipRetry(request)) {
            return chain.proceed(request)
        }

        for (attempt in 0 until maxRetries) {
            try {
                val response = chain.proceed(request)

                // Повторяем только для определенных HTTP кодов
                if (shouldRetryForStatusCode(response.code, attempt)) {
                    val retryDelay = calculateRetryDelay(attempt)
                    
                    AppLogger.d(TAG, "Попытка ${attempt + 1}/$maxRetries: HTTP ${response.code}, повторим через ${retryDelay}ms")

                    response.close() // Закрываем неудачный ответ
                    Thread.sleep(retryDelay)
                    continue // Повторяем запрос
                }

                // Успешный ответ или ошибка, которую не нужно повторять
                return response

            } catch (e: IOException) {
                lastException = e

                // Проверяем, стоит ли повторять для этого типа ошибки
                if (!shouldRetryForException(e, attempt)) {
                    AppLogger.d(TAG, "Ошибка не требует повторной попытки: ${e.javaClass.simpleName}")
                    throw e
                }

                // Последняя попытка - выбрасываем исключение
                if (attempt == maxRetries - 1) {
                    AppLogger.e(TAG, "Исчерпаны все попытки ($maxRetries) для ${request.url}", e)
                    throw e
                }

                val retryDelay = calculateRetryDelay(attempt)
                
                AppLogger.w(TAG, "Попытка ${attempt + 1}/$maxRetries: ${e.javaClass.simpleName} - ${e.message}, повторим через ${retryDelay}ms")

                try {
                    Thread.sleep(retryDelay)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Прервано во время повторной попытки", ie)
                }
            }
        }

        // Если дошли сюда, выбрасываем последнее исключение
        throw lastException ?: IOException("Неизвестная ошибка после $maxRetries попыток")
    }

    /**
     * Определяет, нужно ли пропустить retry для этого запроса
     */
    private fun shouldSkipRetry(request: Request): Boolean {
        // Не повторяем POST/PUT/DELETE запросы (идемпотентность)
        val method = request.method
        return method == "POST" || method == "PUT" || method == "DELETE" || method == "PATCH"
    }

    /**
     * Определяет, нужно ли повторять запрос для HTTP статус кода
     */
    private fun shouldRetryForStatusCode(statusCode: Int, attempt: Int): Boolean {
        return statusCode in AppConstants.RETRYABLE_HTTP_CODES && attempt < maxRetries - 1
    }

    /**
     * Определяет, нужно ли повторять запрос для этого типа исключения
     */
    private fun shouldRetryForException(exception: IOException, attempt: Int): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true // Таймаут - можно повторить
            is ConnectException -> true // Ошибка подключения - можно повторить
            is UnknownHostException -> attempt == 0 // DNS ошибка - только одна попытка
            else -> exception.message?.let { message ->
                // Повторяем для временных сетевых проблем
                message.contains("timeout", ignoreCase = true) ||
                message.contains("unable to resolve host", ignoreCase = true) ||
                message.contains("failed to connect", ignoreCase = true) ||
                message.contains("connection refused", ignoreCase = true)
            } ?: false
        }
    }

    /**
     * Вычисляет задержку перед следующей попыткой с экспоненциальным backoff
     */
    private fun calculateRetryDelay(attempt: Int): Long {
        // Экспоненциальная задержка: 1s, 2s, 4s, ...
        val delay = (initialDelayMs * (1L shl attempt)).coerceAtMost(maxDelayMs)
        return delay
    }
}

