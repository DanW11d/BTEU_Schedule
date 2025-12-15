package com.example.bteu_schedule.widget

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import com.example.bteu_schedule.R

/**
 * A7.7: Утилиты для отображения состояний виджета
 * 
 * Помогает настроить RemoteViews для различных состояний виджета
 */
object WidgetStateUtils {
    
    /**
     * A7.7.1: Нормальное состояние
     * Показывает актуальные данные (расписание, реплику ассистента)
     * 
     * @param remoteViews RemoteViews виджета
     * @param messageText Текст реплики ассистента
     * @param assistantIntent PendingIntent для тапа по реплике
     * @param openButtonIntent PendingIntent для кнопки "Открыть"
     */
    fun setNormalState(
        remoteViews: RemoteViews,
        messageText: String,
        assistantIntent: PendingIntent,
        openButtonIntent: PendingIntent
    ) {
        // Показываем реплику ассистента
        // Примечание: ID виджетов (widget_message_text, widget_open_button и т.д.) 
        // будут определены в layout файлах виджетов (widget_layout.xml)
        // После создания layout файлов раскомментировать следующие строки:
        /*
        remoteViews.setTextViewText(R.id.widget_message_text, messageText)
        remoteViews.setViewVisibility(R.id.widget_message_text, android.view.View.VISIBLE)
        
        // Показываем кнопку "Открыть"
        remoteViews.setViewVisibility(R.id.widget_open_button, android.view.View.VISIBLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_open_button, openButtonIntent)
        
        // Устанавливаем тап по реплике
        remoteViews.setOnClickPendingIntent(R.id.widget_message_text, assistantIntent)
        
        // Скрываем состояния ошибок
        remoteViews.setViewVisibility(R.id.widget_error_container, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_loading_container, android.view.View.GONE)
        */
    }
    
    /**
     * A7.7.2: Нет данных
     * Сообщение: "Недоступно. Обнови расписание."
     * Кнопка: "Обновить"
     * 
     * @param remoteViews RemoteViews виджета
     * @param context Контекст приложения
     * @param refreshIntent PendingIntent для кнопки "Обновить"
     */
    fun setNoDataState(
        remoteViews: RemoteViews,
        context: Context,
        refreshIntent: PendingIntent
    ) {
        // Показываем сообщение об ошибке
        // Примечание: ID виджетов будут определены в layout файлах виджетов
        // После создания layout файлов раскомментировать следующие строки:
        /*
        remoteViews.setTextViewText(
            R.id.widget_error_message,
            context.getString(R.string.widget_no_data_message) // "Недоступно. Обнови расписание."
        )
        remoteViews.setViewVisibility(R.id.widget_error_container, android.view.View.VISIBLE)
        
        // Показываем кнопку "Обновить"
        remoteViews.setTextViewText(
            R.id.widget_error_button,
            context.getString(R.string.widget_refresh_button) // "Обновить"
        )
        remoteViews.setViewVisibility(R.id.widget_error_button, android.view.View.VISIBLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_error_button, refreshIntent)
        
        // Скрываем нормальное состояние
        remoteViews.setViewVisibility(R.id.widget_message_text, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_open_button, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_loading_container, android.view.View.GONE)
        */
    }
    
    /**
     * A7.7.3: Обновление
     * Анимация точек, лёгкий shimmer
     * 
     * @param remoteViews RemoteViews виджета
     * @param context Контекст приложения
     */
    fun setLoadingState(
        remoteViews: RemoteViews,
        context: Context
    ) {
        // Показываем контейнер загрузки
        // Примечание: ID виджетов будут определены в layout файлах виджетов
        // После создания layout файлов раскомментировать следующие строки:
        /*
        remoteViews.setViewVisibility(R.id.widget_loading_container, android.view.View.VISIBLE)
        
        // Устанавливаем текст "Обновление..."
        remoteViews.setTextViewText(
            R.id.widget_loading_text,
            context.getString(R.string.widget_loading_message) // "Обновление..."
        )
        
        // Запускаем анимацию точек (через drawable с анимацией)
        remoteViews.setImageViewResource(
            R.id.widget_loading_dots,
            R.drawable.widget_loading_dots_animation
        )
        
        // Скрываем нормальное состояние и ошибки
        remoteViews.setViewVisibility(R.id.widget_message_text, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_open_button, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_error_container, android.view.View.GONE)
        */
    }
    
    /**
     * A7.7.4: Неправильная группа
     * Сообщение: "Выберите группу в приложении"
     * Кнопка: "Открыть"
     * 
     * @param remoteViews RemoteViews виджета
     * @param context Контекст приложения
     * @param openAppIntent PendingIntent для кнопки "Открыть"
     */
    fun setNoGroupState(
        remoteViews: RemoteViews,
        context: Context,
        openAppIntent: PendingIntent
    ) {
        // Показываем сообщение об ошибке
        // Примечание: ID виджетов будут определены в layout файлах виджетов
        // После создания layout файлов раскомментировать следующие строки:
        /*
        remoteViews.setTextViewText(
            R.id.widget_error_message,
            context.getString(R.string.widget_no_group_message) // "Выберите группу в приложении"
        )
        remoteViews.setViewVisibility(R.id.widget_error_container, android.view.View.VISIBLE)
        
        // Показываем кнопку "Открыть"
        remoteViews.setTextViewText(
            R.id.widget_error_button,
            context.getString(R.string.widget_open_button) // "Открыть"
        )
        remoteViews.setViewVisibility(R.id.widget_error_button, android.view.View.VISIBLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_error_button, openAppIntent)
        
        // Скрываем нормальное состояние
        remoteViews.setViewVisibility(R.id.widget_message_text, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_open_button, android.view.View.GONE)
        remoteViews.setViewVisibility(R.id.widget_loading_container, android.view.View.GONE)
        */
    }
    
    /**
     * Создает PendingIntent для обновления виджета
     */
    fun createRefreshIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = android.content.Intent(
            android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ).apply {
            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REFRESH,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Создает PendingIntent для открытия приложения
     */
    fun createOpenAppIntent(context: Context): PendingIntent {
        val intent = android.content.Intent(context, com.example.bteu_schedule.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private const val REQUEST_CODE_REFRESH = 2000
    private const val REQUEST_CODE_OPEN_APP = 2001
}

