package com.example.bteu_schedule.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.bteu_schedule.R

data class Lesson(val time: String, val name: String, val details: String)

class ScheduleWidgetViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var lessons = listOf<Lesson>()

    override fun onCreate() {
        // Соединение с источником данных
    }

    override fun onDataSetChanged() {
        // Вызывается при обновлении данных. Здесь мы будем загружать расписание.
        // Пока используем тестовые данные
        lessons = listOf(
            Lesson("08:30", "Математика", "Иванов И.И. | 301-4"),
            Lesson("10:10", "Физика", "Петров П.П. | 205-1"),
            Lesson("12:10", "Программирование", "Сидоров С.С. | 404-2")
        )
    }

    override fun onDestroy() {
        // Закрываем соединение с источником данных
    }

    override fun getCount(): Int = lessons.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= lessons.size) {
            return getLoadingView() // Безопасность
        }

        val lesson = lessons[position]
        val views = RemoteViews(context.packageName, R.layout.widget_lesson_item)

        views.setTextViewText(R.id.widget_lesson_time, lesson.time)
        views.setTextViewText(R.id.widget_lesson_name, lesson.name)
        views.setTextViewText(R.id.widget_lesson_details, lesson.details)

        return views
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_lesson_item).apply {
            setTextViewText(R.id.widget_lesson_name, "Загрузка...")
        }
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}