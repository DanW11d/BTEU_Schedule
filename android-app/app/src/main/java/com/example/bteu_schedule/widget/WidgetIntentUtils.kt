package com.example.bteu_schedule.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import com.example.bteu_schedule.MainActivity
import com.example.bteu_schedule.ui.navigation.AppDestinations

/**
 * A7.6: Утилиты для создания PendingIntent для виджетов
 * 
 * Обрабатывает различные типы взаимодействий с виджетом:
 * - Тап по реплике → открывает ассистента
 * - Тап по кнопке "Открыть" → переход в соответствующий экран
 * - Тап по дню недели → открывает расписание дня
 * - Тап по микрофону → открывает режим голосового ассистента
 */
object WidgetIntentUtils {
    
    /**
     * A7.6.1: Тап по реплике → открывает ассистента с диалогом по теме
     * 
     * @param context Контекст приложения
     * @param messageText Текст реплики для передачи ассистенту
     * @return PendingIntent для открытия ассистента
     */
    fun createAssistantIntent(
        context: Context,
        messageText: String
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = WidgetActions.ACTION_WIDGET_CLICK
            putExtra(WidgetActions.EXTRA_ACTION_TYPE, WidgetActions.ACTION_TYPE_OPEN_ASSISTANT)
            putExtra(WidgetActions.EXTRA_MESSAGE_TEXT, messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_ASSISTANT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * A7.6.2: Тап по кнопке "Открыть" → переход в соответствующий экран
     * 
     * @param context Контекст приложения
     * @param actionType Тип действия: open_schedule, open_exams, open_next_lesson
     * @param groupCode Код группы (опционально)
     * @param lessonId ID урока (для open_next_lesson)
     * @return PendingIntent для открытия экрана
     */
    fun createOpenScreenIntent(
        context: Context,
        actionType: String,
        groupCode: String? = null,
        lessonId: Int? = null
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = WidgetActions.ACTION_WIDGET_CLICK
            putExtra(WidgetActions.EXTRA_ACTION_TYPE, actionType)
            if (groupCode != null) {
                putExtra(WidgetActions.EXTRA_GROUP_CODE, groupCode)
            }
            if (lessonId != null) {
                putExtra(WidgetActions.EXTRA_LESSON_ID, lessonId)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val requestCode = when (actionType) {
            WidgetActions.ACTION_TYPE_OPEN_SCHEDULE -> REQUEST_CODE_SCHEDULE
            WidgetActions.ACTION_TYPE_OPEN_EXAMS -> REQUEST_CODE_EXAMS
            WidgetActions.ACTION_TYPE_OPEN_NEXT_LESSON -> REQUEST_CODE_NEXT_LESSON
            else -> REQUEST_CODE_DEFAULT
        }
        
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * A7.6.3: Тап по дню недели → открывает расписание дня
     * 
     * @param context Контекст приложения
     * @param dayOfWeek День недели (1-6: Понедельник-Суббота)
     * @param groupCode Код группы (опционально)
     * @return PendingIntent для открытия расписания дня
     */
    fun createDayScheduleIntent(
        context: Context,
        dayOfWeek: Int,
        groupCode: String? = null
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = WidgetActions.ACTION_WIDGET_CLICK
            putExtra(WidgetActions.EXTRA_ACTION_TYPE, WidgetActions.ACTION_TYPE_OPEN_DAY_SCHEDULE)
            putExtra(WidgetActions.EXTRA_DAY_OF_WEEK, dayOfWeek)
            if (groupCode != null) {
                putExtra(WidgetActions.EXTRA_GROUP_CODE, groupCode)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_DAY_SCHEDULE + dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * A7.6.4: Тап по микрофону → открывает режим голосового ассистента
     * 
     * @param context Контекст приложения
     * @return PendingIntent для открытия голосового ассистента
     */
    fun createVoiceAssistantIntent(
        context: Context
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = WidgetActions.ACTION_WIDGET_CLICK
            putExtra(WidgetActions.EXTRA_ACTION_TYPE, WidgetActions.ACTION_TYPE_OPEN_VOICE_ASSISTANT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_VOICE_ASSISTANT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    // Request codes для различных действий (должны быть уникальными)
    private const val REQUEST_CODE_ASSISTANT = 1000
    private const val REQUEST_CODE_SCHEDULE = 1001
    private const val REQUEST_CODE_EXAMS = 1002
    private const val REQUEST_CODE_NEXT_LESSON = 1003
    private const val REQUEST_CODE_DAY_SCHEDULE = 1010 // Базовый код для дней недели (1010-1015)
    private const val REQUEST_CODE_VOICE_ASSISTANT = 1020
    private const val REQUEST_CODE_DEFAULT = 1999
}

