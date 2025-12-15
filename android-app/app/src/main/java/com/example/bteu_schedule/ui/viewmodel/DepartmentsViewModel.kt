package com.example.bteu_schedule.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.ScheduleRepository
import com.example.bteu_schedule.domain.models.DepartmentUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DepartmentsUiState {
    data object Loading : DepartmentsUiState
    data class Success(val departments: List<DepartmentUi>) : DepartmentsUiState
    data class Error(val message: String) : DepartmentsUiState
    data object Empty : DepartmentsUiState
}

@HiltViewModel
class DepartmentsViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DepartmentsUiState>(DepartmentsUiState.Loading)
    val uiState: StateFlow<DepartmentsUiState> = _uiState.asStateFlow()

    fun loadAllDepartments() {
        viewModelScope.launch {
            _uiState.value = DepartmentsUiState.Loading
            try {
                when (val result = repository.getAllDepartments()) {
                    is ApiResponse.Success -> {
                        if (result.data.isNotEmpty()) {
                            // Группируем по факультетам и сортируем
                            val sortedDepartments = result.data.sortedWith(
                                compareBy<DepartmentUi> { it.facultyCode }
                                    .thenBy { it.name }
                            )
                            _uiState.value = DepartmentsUiState.Success(sortedDepartments)
                        } else {
                            _uiState.value = DepartmentsUiState.Empty
                        }
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = DepartmentsUiState.Error(result.message)
                    }
                    is ApiResponse.Loading -> {
                        // Остаемся в состоянии Loading
                    }
                }
            } catch (e: Exception) {
                Log.e("DepartmentsViewModel", "Ошибка загрузки кафедр", e)
                _uiState.value = DepartmentsUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }
}

