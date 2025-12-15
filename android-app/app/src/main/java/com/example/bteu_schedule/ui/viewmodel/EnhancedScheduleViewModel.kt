package com.example.bteu_schedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.data.repository.ScheduleStatus
import com.example.bteu_schedule.domain.models.*
import com.example.bteu_schedule.domain.service.ScheduleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Расширенный ViewModel для работы с расписанием
 * Использует ScheduleService для работы с датами
 */
@HiltViewModel
class EnhancedScheduleViewModel @Inject constructor(
    private val repository: CachedScheduleRepository,
    private val scheduleService: ScheduleService
) : ViewModel() {
    
    // Статус обновления (заглушка, так как CachedScheduleRepository не имеет status)
    private val _updateStatus = MutableStateFlow<ScheduleStatus>(ScheduleStatus.Updated)
    val updateStatus: StateFlow<ScheduleStatus> = _updateStatus.asStateFlow()
    
    // Время последнего обновления (заглушка)
    val lastUpdate: StateFlow<Long?> = MutableStateFlow(null)
    
    // Факультеты
    private val _faculties = MutableStateFlow<List<FacultyUi>>(emptyList())
    val faculties: StateFlow<List<FacultyUi>> = _faculties.asStateFlow()
    
    // Группы
    private val _groups = MutableStateFlow<List<GroupUi>>(emptyList())
    val groups: StateFlow<List<GroupUi>> = _groups.asStateFlow()
    
    // Расписание
    private val _lessons = MutableStateFlow<List<LessonUi>>(emptyList())
    val lessons: StateFlow<List<LessonUi>> = _lessons.asStateFlow()
    
    // Следующая пара
    private val _nextLesson = MutableStateFlow<LessonUi?>(null)
    val nextLesson: StateFlow<LessonUi?> = _nextLesson.asStateFlow()
    
    // Экзамены
    private val _exams = MutableStateFlow<List<ExamUi>>(emptyList())
    val exams: StateFlow<List<ExamUi>> = _exams.asStateFlow()
    
    // Зачеты
    private val _credits = MutableStateFlow<List<CreditUi>>(emptyList())
    val credits: StateFlow<List<CreditUi>> = _credits.asStateFlow()
    
    init {
        // Загружаем факультеты при инициализации
        viewModelScope.launch {
            repository.getFaculties().collect { faculties ->
                _faculties.value = faculties
            }
        }
    }
    
    /**
     * Загрузить факультеты
     */
    fun loadFaculties() {
        viewModelScope.launch {
            repository.getFaculties().collect { faculties ->
                _faculties.value = faculties
            }
        }
    }
    
    /**
     * Загрузить группы
     */
    fun loadGroups(facultyCode: String, educationForm: String, course: Int) {
        viewModelScope.launch {
            repository.getGroups(facultyCode, educationForm, course).collect { groups ->
                _groups.value = groups
            }
        }
    }
    
    /**
     * Загрузить расписание для группы
     */
    fun loadSchedule(groupCode: String, dayOfWeek: Int, isOddWeek: Boolean) {
        viewModelScope.launch {
            repository.getDaySchedule(groupCode, dayOfWeek, isOddWeek).collect { lessons ->
                _lessons.value = lessons
                // Обновляем следующую пару
                updateNextLesson()
            }
        }
    }
    
    /**
     * Получить расписание на сегодня
     */
    fun getTodayLessons(): List<LessonUi> {
        return scheduleService.getTodayLessons(_lessons.value)
    }
    
    /**
     * Получить расписание на завтра
     */
    fun getTomorrowLessons(): List<LessonUi> {
        return scheduleService.getTomorrowLessons(_lessons.value)
    }
    
    /**
     * Получить расписание на конкретный день недели
     */
    fun getLessonsForDayOfWeek(dayOfWeek: Int, isOddWeek: Boolean? = null): List<LessonUi> {
        return scheduleService.getLessonsForDayOfWeek(_lessons.value, dayOfWeek, isOddWeek)
    }
    
    /**
     * Получить расписание на дату
     */
    fun getLessonsForDate(date: LocalDate): List<LessonUi> {
        return scheduleService.getLessonsForDate(_lessons.value, date)
    }
    
    /**
     * Получить расписание на неделю
     */
    fun getWeekSchedule(startDate: LocalDate = LocalDate.now()): Map<Int, List<LessonUi>> {
        return scheduleService.getWeekSchedule(_lessons.value, startDate)
    }
    
    /**
     * Обновить следующую пару
     */
    private fun updateNextLesson() {
        _nextLesson.value = scheduleService.getNextLesson(_lessons.value)
    }
    
    /**
     * Загрузить экзамены
     */
    fun loadExams(groupCode: String) {
        viewModelScope.launch {
            val response = repository.getExams(groupCode)
            if (response is com.example.bteu_schedule.data.remote.dto.ApiResponse.Success) {
                _exams.value = response.data
            }
        }
    }
    
    /**
     * Загрузить зачеты (заглушка, так как метод отсутствует в CachedScheduleRepository)
     */
    fun loadCredits(groupCode: String) {
        viewModelScope.launch {
            _credits.value = emptyList()
        }
    }
    
    /**
     * Принудительное обновление данных
     */
    fun forceRefresh() {
        viewModelScope.launch {
            repository.syncAllDataFromServer()
        }
    }
    
    /**
     * Обновить данные, если требуется
     */
    fun refreshIfNeeded() {
        viewModelScope.launch {
            repository.syncAllDataFromServer()
        }
    }
}

