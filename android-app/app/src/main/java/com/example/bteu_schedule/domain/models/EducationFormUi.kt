package com.example.bteu_schedule.domain.models

/**
 * UI модель формы обучения
 */
enum class EducationFormUi(val code: String, val title: String, val subtitle: String) {
    FULL_TIME("full_time", "Очная форма", "Дневное обучение с полным расписанием"),
    PART_TIME("part_time", "Заочная форма", "Вечернее обучение с сокращённым расписанием")
}

