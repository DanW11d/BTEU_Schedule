package com.example.bteu_schedule.data.remote.api

import com.example.bteu_schedule.data.remote.dto.OpenAiChatRequest
import com.example.bteu_schedule.data.remote.dto.OpenAiChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API интерфейс для работы с OpenAI ChatGPT API
 */
interface OpenAiApiService {
    
    /**
     * Отправить сообщение в ChatGPT
     * 
     * @param authorization Bearer токен с API ключом (формат: "Bearer sk-...")
     * @param request Запрос с сообщениями
     */
    @POST("chat/completions")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAiChatRequest
    ): Response<OpenAiChatResponse>
}

