package com.example.bteu_schedule.data.local.database.mapper

import com.example.bteu_schedule.data.local.database.entity.*
import com.example.bteu_schedule.domain.models.*

/**
 * Мапперы для преобразования между Room Entity и Domain моделями
 */

// Faculty
fun CachedFaculty.toDomain(): FacultyUi {
    return FacultyUi(
        id = this.id,
        code = this.code,
        name = this.name,
        description = this.description
    )
}

fun FacultyUi.toEntity(): CachedFaculty {
    return CachedFaculty(
        id = this.id,
        code = this.code,
        name = this.name,
        description = this.description
    )
}

// Group
fun CachedGroup.toDomain(): GroupUi {
    return GroupUi(
        code = this.code,
        name = this.name,
        course = this.course,
        specialization = this.specialization,
        facultyCode = this.facultyCode,
        facultyName = this.facultyName,
        educationForm = this.educationForm,
        departmentId = this.departmentId,
        departmentName = this.departmentName
    )
}

fun GroupUi.toEntity(): CachedGroup {
    // Используем "UNKNOWN" как значение по умолчанию для facultyCode, если оно null или пустое
    // Это необходимо, так как CachedGroup теперь требует не-null facultyCode для составного ключа
    val safeFacultyCode = this.facultyCode?.takeIf { it.isNotBlank() && it != "." && !it.startsWith(".") } ?: "UNKNOWN"
    
    // Логируем предупреждение, если facultyCode был пустым или невалидным
    if (this.facultyCode.isNullOrBlank() || this.facultyCode == "." || this.facultyCode.startsWith(".")) {
        android.util.Log.w("CacheMapper", "Группа '${this.code}' имеет невалидный facultyCode: '${this.facultyCode}', используется 'UNKNOWN'")
    }
    
    return CachedGroup(
        code = this.code,
        name = this.name,
        course = this.course,
        specialization = this.specialization,
        facultyCode = safeFacultyCode,
        facultyName = this.facultyName,
        educationForm = this.educationForm,
        departmentId = this.departmentId,
        departmentName = this.departmentName
    )
}

// Lesson
fun CachedLesson.toDomain(): LessonUi {
    return LessonUi(
        id = this.lessonId,
        pairNumber = this.pairNumber,
        dayOfWeek = this.dayOfWeek,
        time = this.time,
        subject = this.subject,
        teacher = this.teacher,
        classroom = this.classroom,
        type = this.type,
        weekParity = this.weekParity
    )
}

fun LessonUi.toEntity(groupCode: String): CachedLesson {
    return CachedLesson(
        id = 0, // Автогенерация
        lessonId = this.id,
        groupCode = groupCode,
        pairNumber = this.pairNumber,
        dayOfWeek = this.dayOfWeek,
        time = this.time,
        subject = this.subject,
        teacher = this.teacher,
        classroom = this.classroom,
        type = this.type,
        weekParity = this.weekParity
    )
}

// Exam
fun CachedExam.toDomain(): ExamUi {
    return ExamUi(
        id = this.examId,
        groupCode = this.groupCode,
        subject = this.subject,
        teacher = this.teacher,
        date = this.date,
        time = this.time,
        classroom = this.classroom,
        examType = this.examType,
        notes = this.notes
    )
}

fun ExamUi.toEntity(): CachedExam {
    return CachedExam(
        id = 0, // Автогенерация
        examId = this.id,
        groupCode = this.groupCode,
        subject = this.subject,
        teacher = this.teacher,
        date = this.date,
        time = this.time,
        classroom = this.classroom,
        examType = this.examType,
        notes = this.notes
    )
}

