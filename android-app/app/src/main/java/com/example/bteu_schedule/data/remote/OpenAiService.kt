package com.example.bteu_schedule.data.remote

import android.content.Context
import android.util.Log
import com.example.bteu_schedule.data.local.OpenAiKeyManager
import com.example.bteu_schedule.data.remote.api.OpenAiApiService
import com.example.bteu_schedule.data.remote.dto.OpenAiChatRequest
import com.example.bteu_schedule.data.remote.dto.OpenAiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Сервис для работы с OpenAI ChatGPT API
 */
class OpenAiService(
    private val context: Context,
    private val apiService: OpenAiApiService = OpenAiClient.openAiApi
) {
    
    /**
     * Получить ответ от ChatGPT
     * 
     * @param userMessage Сообщение пользователя
     * @param groupCode Код группы (для контекста расписания)
     * @param conversationHistory История предыдущих сообщений
     */
    suspend fun getChatResponse(
        userMessage: String,
        groupCode: String? = null,
        conversationHistory: List<Pair<String, String>> = emptyList() // (role, content)
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = OpenAiKeyManager.getApiKey(context)
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("API ключ не настроен. Пожалуйста, введите API ключ в настройках.")
                )
            }
            
            // Формируем системное сообщение с контекстом
            val systemMessage = buildSystemMessage(groupCode)
            
            // Формируем историю сообщений
            val messages = mutableListOf<OpenAiMessage>().apply {
                // Системное сообщение
                add(OpenAiMessage("system", systemMessage))
                
                // История разговора
                conversationHistory.forEach { (role, content) ->
                    add(OpenAiMessage(role, content))
                }
                
                // Текущее сообщение пользователя
                add(OpenAiMessage("user", userMessage))
            }
            
            // Создаем запрос
            val request = OpenAiChatRequest(
                model = "gpt-3.5-turbo", // Используем более дешевую модель для тестирования
                messages = messages,
                maxTokens = 500,
                temperature = 0.7
            )
            
            // Отправляем запрос
            val authorization = "Bearer $apiKey"
            val response = apiService.sendChatMessage(authorization, "application/json", request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val aiResponse = body.choices?.firstOrNull()?.message?.content
                    if (!aiResponse.isNullOrBlank()) {
                        Log.d("OpenAiService", "Получен ответ от ChatGPT: ${aiResponse.take(100)}...")
                        return@withContext Result.success(aiResponse.trim())
                    } else {
                        return@withContext Result.failure(Exception("Пустой ответ от ChatGPT"))
                    }
                } else {
                    return@withContext Result.failure(Exception("Пустое тело ответа"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = response.body()
                val errorMessage = errorResponse?.error?.message ?: errorBody ?: "Неизвестная ошибка"
                val errorType = errorResponse?.error?.type
                val errorCode = errorResponse?.error?.code
                
                Log.e("OpenAiService", "Ошибка API: ${response.code()} - $errorMessage")
                
                // Формируем понятное сообщение об ошибке
                val userFriendlyMessage = when {
                    errorType == "insufficient_quota" || errorCode == "insufficient_quota" -> {
                        "Превышен лимит использования API. " +
                        "Проверьте баланс на https://platform.openai.com/account/billing " +
                        "или пополните счёт."
                    }
                    errorType == "unsupported_country_region_territory" || 
                    errorMessage.contains("unsupported_country", ignoreCase = true) -> {
                        "OpenAI API недоступен в вашем регионе. " +
                        "Попробуйте использовать VPN или обратитесь в поддержку OpenAI."
                    }
                    response.code() == 429 -> {
                        "Слишком много запросов. Подождите немного и попробуйте снова."
                    }
                    response.code() == 401 -> {
                        "Неверный API ключ. Проверьте ключ в настройках."
                    }
                    else -> {
                        "Ошибка ChatGPT API (${response.code()}): $errorMessage"
                    }
                }
                
                return@withContext Result.failure(Exception(userFriendlyMessage))
            }
        } catch (e: Exception) {
            Log.e("OpenAiService", "Ошибка при запросе к ChatGPT", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Проверить, настроен ли API ключ
     */
    fun isConfigured(): Boolean {
        return OpenAiKeyManager.isApiKeyConfigured(context)
    }
    
    /**
     * Построить системное сообщение с контекстом расписания
     */
    private fun buildSystemMessage(groupCode: String?): String {
        return buildString {
            append("Вы - умный AI-ассистент для приложения расписания занятий БТЭУ (Белорусский торгово-экономический университет).\n\n")
            append("Ваши возможности:\n")
            append("1. Анализ расписания: нагрузка на неделю, баланс часов, приоритеты занятий\n")
            append("2. Поиск конкретных занятий: \"Когда у меня следующая пара по матану?\"\n")
            append("3. Планирование: предложения по оптимизации расписания\n")
            append("4. Ответы на вопросы о расписании, преподавателях, аудиториях, экзаменах\n\n")
            append("Стиль ответов:\n")
            append("- Краткие, информативные ответы\n")
            append("- Для вопросов типа \"когда следующая пара по X\" - дайте точный ответ с днем и временем\n")
            append("- Предлагайте рекомендации на основе анализа нагрузки\n")
            append("- Будьте дружелюбны и полезны\n\n")
            append("Формат ответов:\n")
            append("- Для простых вопросов: краткий прямой ответ\n")
            append("- Для аналитики: структурированный ответ с цифрами\n")
            append("- Для планирования: конкретные рекомендации\n")
            
            if (!groupCode.isNullOrBlank()) {
                append("\n\nГруппа пользователя: $groupCode")
            }
        }
    }
}

