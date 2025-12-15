package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для AI чата
 */
data class AiChatRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("group_code")
    val groupCode: String? = null
)

data class AiChatResponse(
    @SerializedName("response")
    val response: String,
    @SerializedName("group_code")
    val groupCode: String? = null
)

data class AiStatusResponse(
    @SerializedName("configured")
    val configured: Boolean,
    @SerializedName("provider")
    val provider: String? = null,
    @SerializedName("message")
    val message: String
)

