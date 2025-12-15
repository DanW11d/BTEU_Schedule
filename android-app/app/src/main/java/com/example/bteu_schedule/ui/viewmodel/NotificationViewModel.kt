package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.NotificationRepository
import com.example.bteu_schedule.domain.models.NotificationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        // Загружаем уведомления с обработкой ошибок
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                when (val result = repository.getNotifications()) {
                    is ApiResponse.Success -> {
                        _uiState.value = NotificationUiState(notifications = result.data)
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = NotificationUiState(
                            notifications = _uiState.value.notifications, // Сохраняем существующие уведомления
                            error = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                // Обрабатываем любые исключения, чтобы избежать крашей
                _uiState.value = NotificationUiState(
                    notifications = _uiState.value.notifications, // Сохраняем существующие уведомления
                    error = "Ошибка загрузки уведомлений: ${e.message}",
                    isLoading = false
                )
                com.example.bteu_schedule.utils.AppLogger.e("NotificationViewModel", "Ошибка загрузки уведомлений", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            // Assuming the repository has a method to mark all as read
            // repository.markAllAsRead()
            // For now, let's just reload the list
            loadNotifications()
        }
    }
}