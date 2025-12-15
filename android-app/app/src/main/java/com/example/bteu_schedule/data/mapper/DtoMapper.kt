package com.example.bteu_schedule.data.mapper

import com.example.bteu_schedule.data.remote.dto.*
import com.example.bteu_schedule.domain.models.*

/**
 * Мапперы для преобразования DTO → Domain модели
 */

fun FacultyDto.toDomain(): FacultyUi {
    return FacultyUi(
        id = this.id,
        code = this.code,
        name = this.name ?: this.nameRu ?: this.code,  // Используем "name" из API, если есть, иначе "name_ru" или "code"
        description = this.description ?: this.code
    )
}

fun DepartmentDto.toDomain(facultyCode: String? = null): DepartmentUi {
    return DepartmentUi(
        id = this.id,
        code = this.code,
        name = this.nameRu,
        facultyCode = facultyCode ?: this.facultyCode ?: "",
        description = this.description
    )
}

fun GroupDto.toDomain(): GroupUi {
    return GroupUi(
        code = this.code,
        name = this.name,
        course = this.course,
        specialization = this.specialization ?: "",
        facultyCode = this.facultyCode,
        facultyName = this.facultyName,
        educationForm = this.educationForm,
        departmentId = this.departmentId,
        departmentName = this.departmentName
    )
}

fun LessonDto.toDomain(bellSchedule: Map<Int, BellScheduleUi>? = null): LessonUi {
    val time = try {
        if (this.timeStart != null && this.timeEnd != null) {
            "${this.timeStart}-${this.timeEnd}"
        } else {
            try {
                bellSchedule?.get(this.lessonNumber)?.let { bell ->
                    val start = bell.lessonStart ?: ""
                    val end = bell.lessonEnd ?: ""
                    if (start.isNotBlank() && end.isNotBlank()) {
                        "$start-$end"
                    } else {
                        ""
                    }
                } ?: ""
            } catch (e: Exception) {
                android.util.Log.e("DtoMapper", "Ошибка получения времени из bellSchedule", e)
                ""
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DtoMapper", "Ошибка формирования времени", e)
        ""
    }
    
    // Нормализуем тип занятия: приводим к нижнему регистру и проверяем различные варианты
    val normalizedType = this.lessonType?.let { type ->
        when (type.lowercase().trim()) {
            "лекция", "lecture", "л" -> "lecture"
            "практика", "practice", "п", "практ" -> "practice"
            "лабораторная", "lab", "лб", "лабораторная работа" -> "lab"
            else -> type.lowercase().trim() // Сохраняем оригинальное значение в нижнем регистре
        }
    } ?: "" // Если null, оставляем пустую строку - это поможет выявить проблему
    
    return LessonUi(
        id = this.id,
        pairNumber = this.lessonNumber,
        dayOfWeek = this.dayOfWeek,
        time = time,
        subject = this.subject,
        teacher = this.teacher ?: "",
        classroom = this.classroom ?: "",
        type = normalizedType,
        weekParity = this.weekParity?.lowercase()?.trim() ?: "both"
    )
}

fun BellScheduleDto.toDomain(): BellScheduleUi {
    return BellScheduleUi(
        lessonNumber = this.lessonNumber,
        lessonStart = this.lessonStart.takeIf { !it.isNullOrBlank() },
        lessonEnd = this.lessonEnd.takeIf { !it.isNullOrBlank() },
        breakTimeMinutes = this.breakTimeMinutes,
        breakAfterLessonMinutes = this.breakAfterLessonMinutes,
        description = this.description
    )
}

fun NotificationDto.toDomain(): NotificationUi {
    return NotificationUi(
        id = this.id,
        title = this.title,
        message = this.message,
        type = this.notificationType ?: "info",
        isRead = this.isRead,
        createdAt = this.createdAt
    )
}

fun ExamDto.toDomain(): ExamUi {
    return ExamUi(
        id = this.id,
        groupCode = this.groupId,
        subject = this.subject,
        teacher = this.teacher,
        date = this.date,
        time = this.time ?: "",
        classroom = this.classroom,
        examType = this.examType,
        notes = this.notes
    )
}

