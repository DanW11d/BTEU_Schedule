package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для запроса к OpenAI API
 */
data class OpenAiChatRequest(
    @SerializedName("model")
    val model: String = "gpt-3.5-turbo",
    
    @SerializedName("messages")
    val messages: List<OpenAiMessage>,
    
    @SerializedName("max_tokens")
    val maxTokens: Int = 500,
    
    @SerializedName("temperature")
    val temperature: Double = 0.7
)

data class OpenAiMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("content")
    val content: String
)

/**
 * DTO для ответа от OpenAI API
 */
data class OpenAiChatResponse(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("object")
    val objectType: String? = null,
    
    @SerializedName("created")
    val created: Long? = null,
    
    @SerializedName("model")
    val model: String? = null,
    
    @SerializedName("choices")
    val choices: List<OpenAiChoice>? = null,
    
    @SerializedName("usage")
    val usage: OpenAiUsage? = null,
    
    @SerializedName("error")
    val error: OpenAiError? = null
)

data class OpenAiChoice(
    @SerializedName("index")
    val index: Int? = null,
    
    @SerializedName("message")
    val message: OpenAiMessage? = null,
    
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class OpenAiUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)

data class OpenAiError(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("code")
    val code: String? = null
)

