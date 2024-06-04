package com.alvdela.smartspend.ui.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService
import com.alvdela.smartspend.util.Constants.WIDGET_TASK

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val taskType = intent.getStringExtra(WIDGET_TASK)
        Log.d("TaskWidgetService", "Creating factory for task type: $taskType")
        return TaskWidgetFactory(this.applicationContext, intent)
    }
}
