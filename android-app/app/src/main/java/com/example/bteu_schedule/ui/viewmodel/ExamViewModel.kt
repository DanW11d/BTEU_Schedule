package com.example.bteu_schedule.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.domain.models.ExamUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExamUiState {
    data object Loading : ExamUiState
    data class Success(val exams: List<ExamUi>) : ExamUiState
    data class Error(val message: String) : ExamUiState
    data object Empty : ExamUiState
}

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: CachedScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    fun loadExams(groupCode: String) {
        if (groupCode.isBlank()) {
            _uiState.value = ExamUiState.Error("Код группы не указан")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExamUiState.Loading
            try {
                when (val result = repository.getExams(groupCode)) {
                    is ApiResponse.Success<List<ExamUi>> -> {
                        // Фильтруем только экзамены (examType == "exam")
                        val filteredExams: List<ExamUi> = result.data.filter { exam: ExamUi ->
                            exam.examType?.lowercase() == "exam" ||
                            (exam.examType == null && !exam.subject.lowercase().contains("зачет") && !exam.subject.lowercase().contains("зачёт"))
                        }
                        
                        if (filteredExams.isNotEmpty()) {
                            _uiState.value = ExamUiState.Success(filteredExams)
                        } else {
                            _uiState.value = ExamUiState.Empty
                        }
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = ExamUiState.Error(result.message)
                    }
                    is ApiResponse.Loading -> {
                        // Остаемся в состоянии Loading
                    }
                }
            } catch (e: Exception) {
                Log.e("ExamViewModel", "Ошибка загрузки экзаменов", e)
                _uiState.value = ExamUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: CachedScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    fun loadTests(groupCode: String) {
        if (groupCode.isBlank()) {
            _uiState.value = ExamUiState.Error("Код группы не указан")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExamUiState.Loading
            try {
                when (val result = repository.getTests(groupCode)) {
                    is ApiResponse.Success<List<ExamUi>> -> {
                        // Фильтруем только зачеты (examType == "test")
                        val filteredTests: List<ExamUi> = result.data.filter { exam: ExamUi ->
                            exam.examType?.lowercase() == "test" || 
                            exam.examType == null || // Если тип не указан, считаем зачетом
                            exam.subject.lowercase().contains("зачет") ||
                            exam.subject.lowercase().contains("зачёт")
                        }
                        
                        if (filteredTests.isNotEmpty()) {
                            _uiState.value = ExamUiState.Success(filteredTests)
                        } else {
                            _uiState.value = ExamUiState.Empty
                        }
                    }
                    is ApiResponse.Error -> {
                        _uiState.value = ExamUiState.Error(result.message)
                    }
                    is ApiResponse.Loading -> {
                        // Остаемся в состоянии Loading
                    }
                }
            } catch (e: Exception) {
                Log.e("TestViewModel", "Ошибка загрузки зачетов", e)
                _uiState.value = ExamUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }
}

