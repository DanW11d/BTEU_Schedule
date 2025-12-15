package com.example.bteu_schedule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeeklyLoadDto(
    @SerializedName("total_lessons")
    val totalLessons: Int,
    @SerializedName("lessons_by_day")
    val lessonsByDay: Map<Int, Int>,
    @SerializedName("lessons_by_type")
    val lessonsByType: Map<String, Int>,
    @SerializedName("total_hours")
    val totalHours: Double,
    @SerializedName("average_per_day")
    val averagePerDay: Double
)

data class HourBalanceDto(
    @SerializedName("lecture_hours")
    val lectureHours: Double,
    @SerializedName("practice_hours")
    val practiceHours: Double,
    @SerializedName("lab_hours")
    val labHours: Double,
    @SerializedName("balance_score")
    val balanceScore: Double,
    @SerializedName("recommendations")
    val recommendations: List<String>
)

data class PriorityLessonDto(
    @SerializedName("day_of_week")
    val dayOfWeek: Int,
    @SerializedName("lesson_number")
    val lessonNumber: Int,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("teacher")
    val teacher: String?,
    @SerializedName("classroom")
    val classroom: String?,
    @SerializedName("lesson_type")
    val lessonType: String?
)

data class UpcomingExamDto(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("days_until")
    val daysUntil: Int
)

data class PrioritiesDto(
    @SerializedName("high_priority")
    val highPriority: List<PriorityLessonDto>,
    @SerializedName("medium_priority")
    val mediumPriority: List<PriorityLessonDto>,
    @SerializedName("upcoming_exams")
    val upcomingExams: List<UpcomingExamDto>
)

data class OptimizationDto(
    @SerializedName("suggestions")
    val suggestions: List<String>,
    @SerializedName("conflicts")
    val conflicts: List<String>,
    @SerializedName("optimization_score")
    val optimizationScore: Double
)

data class ScheduleAnalyticsDto(
    @SerializedName("group_code")
    val groupCode: String,
    @SerializedName("weekly_load")
    val weeklyLoad: WeeklyLoadDto,
    @SerializedName("hour_balance")
    val hourBalance: HourBalanceDto,
    @SerializedName("priorities")
    val priorities: PrioritiesDto,
    @SerializedName("optimization")
    val optimization: OptimizationDto
)

