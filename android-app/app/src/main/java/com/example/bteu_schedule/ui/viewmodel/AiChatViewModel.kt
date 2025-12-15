package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.local.OpenAiKeyManager
import com.example.bteu_schedule.data.remote.OpenAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Сообщение в чате
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UI состояние AI чата
 */
sealed class AiChatUiState {
    object Idle : AiChatUiState()
    object Loading : AiChatUiState()
    data class Success(val messages: List<ChatMessage>) : AiChatUiState()
    data class Error(val message: String) : AiChatUiState()
}

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val openAiService: OpenAiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AiChatUiState>(AiChatUiState.Idle)
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isAiConfigured = MutableStateFlow<Boolean?>(null)
    val isAiConfigured: StateFlow<Boolean?> = _isAiConfigured.asStateFlow()
    
    // История разговора для контекста
    private val conversationHistory = mutableListOf<Pair<String, String>>()
    
    init {
        checkAiStatus()
        // Добавляем приветственное сообщение
        addWelcomeMessage()
    }
    
    /**
     * Проверка статуса AI сервиса (проверяет наличие API ключа)
     */
    fun checkAiStatus() {
        _isAiConfigured.value = openAiService.isConfigured()
    }
    
    /**
     * Отправить сообщение в AI
     */
    fun sendMessage(message: String, groupCode: String? = null) {
        if (message.isBlank()) return
        
        // Проверяем, настроен ли API ключ
        if (!openAiService.isConfigured()) {
            val errorMessage = ChatMessage(
                "API ключ не настроен. Пожалуйста, введите API ключ в настройках приложения.",
                isUser = false
            )
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(ChatMessage(message, isUser = true))
            updatedMessages.add(errorMessage)
            _messages.value = updatedMessages
            _uiState.value = AiChatUiState.Error("API ключ не настроен")
            return
        }
        
        // Добавляем сообщение пользователя
        val userMessage = ChatMessage(message, isUser = true)
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages
        _uiState.value = AiChatUiState.Success(currentMessages)
        
        // Добавляем в историю разговора
        conversationHistory.add("user" to message)
        
        // Отправляем запрос к ChatGPT
        viewModelScope.launch {
            _uiState.value = AiChatUiState.Loading
            
            val result = openAiService.getChatResponse(
                userMessage = message,
                groupCode = groupCode,
                conversationHistory = conversationHistory.dropLast(1) // Исключаем текущее сообщение
            )
            
            if (result.isSuccess) {
                val aiResponse = result.getOrNull() ?: "Извините, не удалось получить ответ."
                val aiMessage = ChatMessage(aiResponse, isUser = false)
                
                // Добавляем ответ в историю
                conversationHistory.add("assistant" to aiResponse)
                
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(aiMessage)
                _messages.value = updatedMessages
                _uiState.value = AiChatUiState.Success(updatedMessages)
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = "Ошибка: ${error?.message ?: "Неизвестная ошибка"}"
                val errorChatMessage = ChatMessage(errorMessage, isUser = false)
                
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(errorChatMessage)
                _messages.value = updatedMessages
                _uiState.value = AiChatUiState.Error(errorMessage)
            }
        }
    }
    
    /**
     * Очистить историю сообщений
     */
    fun clearMessages() {
        _messages.value = emptyList()
        conversationHistory.clear()
        addWelcomeMessage()
        _uiState.value = AiChatUiState.Idle
    }
    
    /**
     * Обновить статус AI (вызывается после настройки API ключа)
     */
    fun refreshAiStatus() {
        checkAiStatus()
    }
    
    /**
     * Добавить приветственное сообщение
     */
    private fun addWelcomeMessage() {
        val isConfigured = openAiService.isConfigured()
        val welcomeText = if (isConfigured) {
            "Привет! Я AI-ассистент для расписания занятий БТЭУ. " +
                    "Задайте мне вопрос о расписании, и я постараюсь помочь! " +
                    "Например: \"Какие занятия у меня завтра?\" или \"Где находится аудитория 101?\""
        } else {
            "Привет! Я AI-ассистент для расписания занятий БТЭУ. " +
                    "Для начала работы необходимо настроить API ключ ChatGPT. " +
                    "Перейдите в Настройки → ChatGPT API и введите ваш API ключ."
        }
        val welcomeMessage = ChatMessage(
            text = welcomeText,
            isUser = false
        )
        _messages.value = listOf(welcomeMessage)
    }
}

