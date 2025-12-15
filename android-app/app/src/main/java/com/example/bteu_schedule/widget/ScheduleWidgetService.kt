package com.example.bteu_schedule.widget

import android.content.Intent
import android.widget.RemoteViewsService

class ScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ScheduleWidgetViewsFactory(this.applicationContext)
    }
}