package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.ScheduleRepository
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.ui.screens.SearchFilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val allResults: List<LessonUi> = emptyList(),
    val filteredResults: List<LessonUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentQuery: String = ""
    private var currentFilter: SearchFilterType = SearchFilterType.ALL

    fun onSearchQueryChange(query: String, groupCode: String?) {
        currentQuery = query
        searchLessons(query, groupCode)
    }
    
    fun onFilterChange(filter: SearchFilterType) {
        currentFilter = filter
        applyFilter()
    }

    private fun searchLessons(query: String, groupCode: String?) {
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.update { SearchUiState() }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = repository.searchLessons(query, groupCode)) {
                    is ApiResponse.Success -> {
                        _uiState.update { 
                            it.copy(
                                allResults = result.data,
                                isLoading = false
                            ) 
                        }
                        applyFilter()
                    }
                    is ApiResponse.Error -> {
                         _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is ApiResponse.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private fun applyFilter() {
        val all = _uiState.value.allResults
        val filtered = if (currentFilter == SearchFilterType.ALL) {
            all
        } else {
            val lowerQuery = currentQuery.lowercase()
            all.filter { lesson ->
                when (currentFilter) {
                    SearchFilterType.SUBJECT -> lesson.subject.lowercase().contains(lowerQuery)
                    SearchFilterType.TEACHER -> lesson.teacher.lowercase().contains(lowerQuery)
                    SearchFilterType.CLASSROOM -> lesson.classroom.lowercase().contains(lowerQuery)
                    SearchFilterType.ALL -> true
                }
            }
        }
        _uiState.update { it.copy(filteredResults = filtered) }
    }
}