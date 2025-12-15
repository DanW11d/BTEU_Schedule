package com.example.bteu_schedule.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.ScheduleRepository
import com.example.bteu_schedule.domain.models.BellScheduleUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BellScheduleUiState {
    data object Loading : BellScheduleUiState
    data class Success(val schedule: List<BellScheduleUi>) : BellScheduleUiState
    data class Error(val message: String) : BellScheduleUiState
    data object Empty : BellScheduleUiState
}

@HiltViewModel
class BellScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BellScheduleUiState>(BellScheduleUiState.Loading)
    val uiState: StateFlow<BellScheduleUiState> = _uiState.asStateFlow()

    fun loadBellSchedule() {
        viewModelScope.launch {
            _uiState.value = BellScheduleUiState.Loading
            try {
                when (val result = repository.getBellSchedule()) {
                    is ApiResponse.Success -> {
                        if (result.data.isNotEmpty()) {
                            // Сортируем по номеру пары
                            val sortedSchedule = result.data.sortedBy { it.lessonNumber }
                            _uiState.value = BellScheduleUiState.Success(sortedSchedule)
                        } else {
                            _uiState.value = BellScheduleUiState.Empty
                        }
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = BellScheduleUiState.Error(result.message)
                    }
                    is ApiResponse.Loading -> {
                        // Остаемся в состоянии Loading
                    }
                }
            } catch (e: Exception) {
                Log.e("BellScheduleViewModel", "Ошибка загрузки расписания звонков", e)
                _uiState.value = BellScheduleUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }
}

