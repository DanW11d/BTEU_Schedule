package com.example.bteu_schedule.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.domain.models.FacultyUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FacultyUiState {
    data object Loading : FacultyUiState
    data class Success(val faculties: List<FacultyUi>) : FacultyUiState
    data class Error(val message: String) : FacultyUiState
}

@HiltViewModel
class FacultySelectionViewModel @Inject constructor(
    private val repository: CachedScheduleRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _syncError = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<FacultyUiState> = combine(
        _refreshTrigger.flatMapLatest { repository.getFaculties() },
        _syncError
    ) { faculties, syncError ->
        when {
            syncError != null -> FacultyUiState.Error(syncError)
            // Показываем данные из кэша сразу, даже если их мало или они невалидные
            // Синхронизацию запускаем в фоне через onEach
            faculties.isEmpty() -> {
                // Если данных совсем нет, показываем Loading
                FacultyUiState.Loading
            }
            faculties.size < 3 || 
            faculties.any { 
                val code = it.code
                code.startsWith(".") || 
                code.matches(Regex("^\\.[0-9]+$")) ||
                code.length > 10 || 
                (code.length == 1 && code.all { it.isDigit() })
            } -> {
                // Показываем данные, даже если они неполные или невалидные
                // Синхронизация запустится в фоне через onEach
                FacultyUiState.Success(faculties)
            }
            else -> {
                _syncError.value = null
                FacultyUiState.Success(faculties)
            }
        }
    }
        .onEach { state ->
            // Запускаем синхронизацию в фоне, если нужно
            if (state is FacultyUiState.Success || state is FacultyUiState.Loading) {
                val faculties = (state as? FacultyUiState.Success)?.faculties ?: emptyList()
                val needsSync = faculties.isEmpty() || 
                    faculties.size < 3 || 
                    faculties.any { 
                        val code = it.code
                        code.startsWith(".") || 
                        code.matches(Regex("^\\.[0-9]+$")) ||
                        code.length > 10 || 
                        (code.length == 1 && code.all { it.isDigit() })
                    }
                
                if (needsSync && !_isSyncing.value) {
                    startBackgroundSync(faculties)
                }
            }
        }
        .catch { 
            emit(FacultyUiState.Error(it.message ?: "Неизвестная ошибка"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FacultyUiState.Loading)
    
    private fun startBackgroundSync(faculties: List<FacultyUi>) {
        if (_isSyncing.value) return // Уже синхронизируем
        
        _isSyncing.value = true
        viewModelScope.launch {
            try {
                val hasInvalidData = faculties.any { 
                    it.code.startsWith(".") || it.code.matches(Regex("^\\.[0-9]+$")) 
                }
                if (hasInvalidData) {
                    Log.w("FacultySelectionViewModel", "Обнаружены неправильные данные факультетов, очищаем базу...")
                    repository.clearAllData()
                }
                Log.d("FacultySelectionViewModel", "Запуск фоновой синхронизации...")
                val syncResult = repository.syncAllDataFromServer()
                if (syncResult is ApiResponse.Error) {
                    _syncError.value = syncResult.message
                    Log.e("FacultySelectionViewModel", "Ошибка синхронизации: ${syncResult.message}")
                } else {
                    _syncError.value = null
                    // Обновляем данные после успешной синхронизации
                    _refreshTrigger.value++
                }
            } catch (e: Exception) {
                val errorMsg = "Ошибка загрузки данных: ${e.message ?: "Неизвестная ошибка"}"
                _syncError.value = errorMsg
                Log.e("FacultySelectionViewModel", "Ошибка синхронизации", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun loadFaculties() {
        _syncError.value = null
        _refreshTrigger.value++
    }
    
    init {
        // Загружаем данные при инициализации
        // Синхронизация запустится автоматически через onEach в uiState, если нужно
        loadFaculties()
    }
}
