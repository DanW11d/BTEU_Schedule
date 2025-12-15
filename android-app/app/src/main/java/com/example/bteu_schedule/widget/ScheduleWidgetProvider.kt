package com.example.bteu_schedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.bteu_schedule.R
import java.util.Calendar

// Actions for button clicks
private const val ACTION_PREV_DAY = "com.example.bteu_schedule.widget.ACTION_PREV_DAY"
private const val ACTION_NEXT_DAY = "com.example.bteu_schedule.widget.ACTION_NEXT_DAY"
private const val ACTION_DAY_CLICK = "com.example.bteu_schedule.widget.ACTION_DAY_CLICK"
private const val ACTION_REFRESH = "com.example.bteu_schedule.widget.ACTION_REFRESH"
private const val EXTRA_DAY_OF_WEEK = "com.example.bteu_schedule.widget.EXTRA_DAY_OF_WEEK"

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        var dayOfWeek = getDayOfWeek(context, appWidgetId)

        when (intent.action) {
            ACTION_PREV_DAY -> dayOfWeek = if (dayOfWeek > Calendar.MONDAY) dayOfWeek - 1 else Calendar.SATURDAY
            ACTION_NEXT_DAY -> dayOfWeek = if (dayOfWeek < Calendar.SATURDAY) dayOfWeek + 1 else Calendar.MONDAY
            ACTION_DAY_CLICK -> dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, dayOfWeek)
            ACTION_REFRESH -> { /* Just force update */ }
        }

        saveDayOfWeek(context, appWidgetId, dayOfWeek)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        // Notify the list view to update its data
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_lessons_list)
        // Re-render the widget with new title and button states
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val dayOfWeek = getDayOfWeek(context, appWidgetId)
        val views = RemoteViews(context.packageName, R.layout.widget_schedule)

        // Set up the intent for the RemoteViewsService
        val serviceIntent = Intent(context, ScheduleWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME)) // Make intent unique
        }
        views.setRemoteAdapter(R.id.widget_lessons_list, serviceIntent)
        views.setEmptyView(R.id.widget_lessons_list, R.id.widget_empty_view)

        // Set titles and button states
        views.setTextViewText(R.id.widget_day_title, getDayTitle(dayOfWeek))
        updateDaySelection(context, views, dayOfWeek)

        // Set up pending intents for buttons
        views.setOnClickPendingIntent(R.id.widget_prev_day_button, getPendingIntent(context, appWidgetId, ACTION_PREV_DAY))
        views.setOnClickPendingIntent(R.id.widget_next_day_button, getPendingIntent(context, appWidgetId, ACTION_NEXT_DAY))
        views.setOnClickPendingIntent(R.id.widget_refresh_button, getPendingIntent(context, appWidgetId, ACTION_REFRESH))

        val dayButtons = mapOf(
            R.id.widget_day_pn to Calendar.MONDAY,
            R.id.widget_day_vt to Calendar.TUESDAY,
            R.id.widget_day_sr to Calendar.WEDNESDAY,
            R.id.widget_day_ct to Calendar.THURSDAY,
            R.id.widget_day_pt to Calendar.FRIDAY,
            R.id.widget_day_sb to Calendar.SATURDAY
        )

        for ((buttonId, day) in dayButtons) {
            val dayClickIntent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                action = ACTION_DAY_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_DAY_OF_WEEK, day)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME) + "/day=$day") // Unique data
            }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId * 10 + day, dayClickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(buttonId, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
        val intent = Intent(context, ScheduleWidgetProvider::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME)) // Make intent unique for this appWidgetId
        }
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    
    private fun updateDaySelection(context: Context, views: RemoteViews, selectedDay: Int) {
        val days = mapOf(
            Calendar.MONDAY to R.id.widget_day_pn,
            Calendar.TUESDAY to R.id.widget_day_vt,
            Calendar.WEDNESDAY to R.id.widget_day_sr,
            Calendar.THURSDAY to R.id.widget_day_ct,
            Calendar.FRIDAY to R.id.widget_day_pt,
            Calendar.SATURDAY to R.id.widget_day_sb
        )
        // Reset all to secondary
        days.values.forEach { id ->
            views.setTextColor(id, context.getColor(R.color.widget_text_secondary))
        }
        // Highlight selected
        days[selectedDay]?.let {
            views.setTextColor(it, context.getColor(R.color.widget_text_highlight))
        }
    }
    
    private fun getDayTitle(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "ПОНЕДЕЛЬНИК"
            Calendar.TUESDAY -> "ВТОРНИК"
            Calendar.WEDNESDAY -> "СРЕДА"
            Calendar.THURSDAY -> "ЧЕТВЕРГ"
            Calendar.FRIDAY -> "ПЯТНИЦА"
            Calendar.SATURDAY -> "СУББОТА"
            else -> "ВОСКРЕСЕНЬЕ"
        }
    }
    
    // --- State Management --- //
    private fun saveDayOfWeek(context: Context, appWidgetId: Int, day: Int) {
        context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE).edit().putInt("day_$appWidgetId", day).apply()
    }

    private fun getDayOfWeek(context: Context, appWidgetId: Int): Int {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return prefs.getInt("day_$appWidgetId", if (today in Calendar.MONDAY..Calendar.SATURDAY) today else Calendar.MONDAY)
    }
}