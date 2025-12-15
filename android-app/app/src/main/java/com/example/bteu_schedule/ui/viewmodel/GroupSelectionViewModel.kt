package com.example.bteu_schedule.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.domain.models.GroupUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GroupUiState {
    data object Loading : GroupUiState
    data class Success(val groups: List<GroupUi>) : GroupUiState
    data class Error(val message: String) : GroupUiState
    data object Empty : GroupUiState
    data object Syncing : GroupUiState // Состояние синхронизации
}

@HiltViewModel
class GroupSelectionViewModel @Inject constructor(
    private val repository: CachedScheduleRepository
) : ViewModel() {

    private val _loadParams = MutableStateFlow<Triple<String?, String?, Int?>>(Triple(null, null, null))
    private val _isSyncing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GroupUiState> = combine(
        _loadParams,
        _isSyncing
    ) { (faculty, form, course), isSyncing ->
        if (isSyncing) {
            GroupUiState.Syncing
        } else if (faculty.isNullOrBlank() || form.isNullOrBlank() || course == null || course <= 0) {
            GroupUiState.Empty
        } else {
            null // Будет обработано в flatMapLatest
        }
    }
        .flatMapLatest { combinedState ->
            if (combinedState != null) {
                flowOf(combinedState)
            } else {
                val (faculty, form, course) = _loadParams.value
                repository.getGroups(faculty!!, form!!, course!!)
                    .map<List<GroupUi>, GroupUiState> { 
                        if (it.isEmpty()) {
                            GroupUiState.Empty
                        } else {
                            GroupUiState.Success(it)
                        }
                    }
                    .catch { emit(GroupUiState.Error(it.message ?: "Unknown error")) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupUiState.Loading)

    fun loadGroups(facultyCode: String?, educationFormCode: String?, course: Int?) {
        _loadParams.value = Triple(facultyCode, educationFormCode, course)
    }

    fun syncData() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                Log.d("GroupSelectionViewModel", "Начало синхронизации данных с FTP...")
                val result = repository.syncAllDataFromServer()
                if (result is ApiResponse.Success) {
                    Log.d("GroupSelectionViewModel", "Синхронизация успешна, перезагружаем группы...")
                    // Перезагружаем группы после синхронизации
                    val (faculty, form, course) = _loadParams.value
                    if (!faculty.isNullOrBlank() && !form.isNullOrBlank() && course != null && course > 0) {
                        loadGroups(faculty, form, course)
                    }
                } else {
                    Log.e("GroupSelectionViewModel", "Ошибка синхронизации: ${(result as? ApiResponse.Error)?.message}")
                }
            } catch (e: Exception) {
                Log.e("GroupSelectionViewModel", "Ошибка при синхронизации", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}