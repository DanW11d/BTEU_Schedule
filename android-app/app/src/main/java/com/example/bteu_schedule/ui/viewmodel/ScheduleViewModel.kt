package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.domain.models.LessonUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data class Success(val lessons: List<LessonUi>) : ScheduleUiState
    data class Error(val message: String) : ScheduleUiState
    data object Empty : ScheduleUiState
}

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: CachedScheduleRepository
) : ViewModel() {

    private val _loadParams = MutableStateFlow<Triple<String, Int, Boolean>>(Triple("", 0, false))
    private val _forceRefresh = MutableStateFlow(0) // Используем счетчик для принудительного обновления

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScheduleUiState> = combine(
        _loadParams,
        _forceRefresh
    ) { (group, day, isOdd), _ ->
        Triple(group, day, isOdd)
    }
        .flatMapLatest { (group, day, isOdd) ->
            if (group.isBlank()) {
                flowOf(ScheduleUiState.Empty)
            } else {
                // Используем forceRefresh=true если был вызван refreshSchedule()
                val forceRefresh = _forceRefresh.value > 0
                repository.getDaySchedule(group, day, isOdd, forceRefresh)
                    .map<List<LessonUi>, ScheduleUiState> { if (it.isEmpty()) ScheduleUiState.Empty else ScheduleUiState.Success(it) }
                    .catch { emit(ScheduleUiState.Error(it.message ?: "Unknown error")) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState.Loading)

    fun loadSchedule(groupCode: String, dayOfWeek: Int, isOddWeek: Boolean) {
        _loadParams.value = Triple(groupCode, dayOfWeek, isOddWeek)
    }
    
    /**
     * Принудительно обновить расписание с FTP сервера
     */
    fun refreshSchedule() {
        _forceRefresh.value = _forceRefresh.value + 1
        // Также перезагружаем текущие параметры для обновления
        val current = _loadParams.value
        if (current.first.isNotBlank()) {
            _loadParams.value = Triple(current.first, current.second, current.third)
        }
    }
}