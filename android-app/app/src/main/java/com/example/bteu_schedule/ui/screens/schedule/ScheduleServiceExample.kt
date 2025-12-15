package com.example.bteu_schedule.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.domain.service.ScheduleService
import com.example.bteu_schedule.ui.viewmodel.EnhancedScheduleViewModel
import com.example.bteu_schedule.utils.WeekCalculator
import java.time.LocalDate

/**
 * Пример использования ScheduleService в UI
 * Демонстрирует различные функции работы с расписанием
 */
@Composable
fun ScheduleServiceExample(
    viewModel: EnhancedScheduleViewModel,
    groupCode: String,
    modifier: Modifier = Modifier
) {
    val scheduleService = remember { ScheduleService() }
    val lessons by viewModel.lessons.collectAsState()
    val nextLesson by viewModel.nextLesson.collectAsState()
    
    // Загружаем расписание при первом показе (для текущего дня недели)
    LaunchedEffect(groupCode) {
        val currentDayOfWeek = LocalDate.now().dayOfWeek.value // 1=Monday, 7=Sunday
        val isOddWeek = WeekCalculator.isCurrentWeekOdd()
        // Загружаем расписание для понедельника (день 1), чтобы показать полную неделю
        viewModel.loadSchedule(groupCode, dayOfWeek = 1, isOddWeek = isOddWeek)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Следующая пара
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Следующая пара",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (nextLesson != null) {
                    LessonInfo(lesson = nextLesson!!)
                } else {
                    Text(
                        text = "Занятий больше нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Расписание на сегодня
        DayScheduleCard(
            title = "Сегодня",
            lessons = scheduleService.getTodayLessons(lessons)
        )
        
        // Расписание на завтра
        DayScheduleCard(
            title = "Завтра",
            lessons = scheduleService.getTomorrowLessons(lessons)
        )
        
        // Расписание на понедельник
        DayScheduleCard(
            title = "Понедельник",
            lessons = scheduleService.getLessonsForDayOfWeek(lessons, 1)
        )
        
        // Расписание на неделю
        WeekScheduleCard(
            schedule = scheduleService.getWeekSchedule(lessons),
            scheduleService = scheduleService
        )
    }
}

@Composable
private fun DayScheduleCard(
    title: String,
    lessons: List<LessonUi>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (lessons.isEmpty()) {
                Text(
                    text = "Занятий нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                lessons.forEach { lesson ->
                    LessonInfo(lesson = lesson)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WeekScheduleCard(
    schedule: Map<Int, List<LessonUi>>,
    scheduleService: ScheduleService
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Расписание на неделю",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            (1..6).forEach { dayOfWeek ->
                val dayLessons = schedule[dayOfWeek] ?: emptyList()
                if (dayLessons.isNotEmpty()) {
                    Text(
                        text = scheduleService.getDayOfWeekName(dayOfWeek),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    dayLessons.forEach { lesson ->
                        LessonInfo(lesson = lesson)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LessonInfo(lesson: LessonUi) {
    Column {
        Text(
            text = "${lesson.pairNumber} пара: ${lesson.subject}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Время: ${lesson.time}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Аудитория: ${lesson.classroom}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (lesson.teacher.isNotBlank()) {
            Text(
                text = "Преподаватель: ${lesson.teacher}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

