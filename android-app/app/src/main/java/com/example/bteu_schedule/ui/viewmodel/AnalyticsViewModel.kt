package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val analytics: com.example.bteu_schedule.data.remote.dto.ScheduleAnalyticsDto) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val apiService: ScheduleApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    fun loadAnalytics(groupCode: String) {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            
            try {
                val response = apiService.getScheduleAnalytics(groupCode)
                
                if (response.isSuccessful) {
                    val analytics = response.body()
                    if (analytics != null) {
                        _uiState.value = AnalyticsUiState.Success(analytics)
                    } else {
                        _uiState.value = AnalyticsUiState.Error("Данные аналитики не получены")
                    }
                } else {
                    _uiState.value = AnalyticsUiState.Error("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error("Ошибка подключения: ${e.message}")
            }
        }
    }
}

