package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.ScheduleRepository
import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.domain.models.DepartmentUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val faculties: List<FacultyUi> = emptyList(),
    val departments: List<DepartmentUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadFaculties() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getFaculties()) {
                is ApiResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        faculties = result.data,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResponse.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun loadDepartments(facultyId: Int, facultyCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getDepartments(facultyId, facultyCode)) {
                is ApiResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        departments = result.data,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResponse.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
}