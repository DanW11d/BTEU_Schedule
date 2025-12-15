package com.example.bteu_schedule.widget

/**
 * A7.6: Действия виджета для обработки взаимодействий
 * 
 * Константы для Intent actions и extras
 */
object WidgetActions {
    // Базовое действие для всех действий виджета
    const val ACTION_WIDGET_CLICK = "com.example.bteu_schedule.WIDGET_CLICK"
    
    // Типы действий
    const val ACTION_TYPE_OPEN_ASSISTANT = "open_assistant"
    const val ACTION_TYPE_OPEN_SCHEDULE = "open_schedule"
    const val ACTION_TYPE_OPEN_EXAMS = "open_exams"
    const val ACTION_TYPE_OPEN_NEXT_LESSON = "open_next_lesson"
    const val ACTION_TYPE_OPEN_DAY_SCHEDULE = "open_day_schedule"
    const val ACTION_TYPE_OPEN_VOICE_ASSISTANT = "open_voice_assistant"
    
    // Extras для передачи данных
    const val EXTRA_ACTION_TYPE = "action_type"
    const val EXTRA_DAY_OF_WEEK = "day_of_week" // 1-6 (Понедельник-Суббота)
    const val EXTRA_MESSAGE_TEXT = "message_text" // Текст реплики для ассистента
    const val EXTRA_LESSON_ID = "lesson_id" // ID следующего урока
    const val EXTRA_GROUP_CODE = "group_code" // Код группы
}

