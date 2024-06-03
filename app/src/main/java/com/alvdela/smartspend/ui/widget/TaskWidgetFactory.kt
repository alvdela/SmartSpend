package com.alvdela.smartspend.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.util.Constants
import java.math.BigDecimal

class TaskWidgetFactory(private val context: Context, private val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    private val filteredTasks: MutableList<Task> = mutableListOf()

    private var taskType: String = intent.getStringExtra(Constants.WIDGET_TASK).orEmpty()

    init {
        Log.d("TaskWidgetFactory", "Task type received: $taskType")
    }

    override fun onCreate() {
        getTasks()
    }

    override fun onDataSetChanged() {
        getTasks()
    }

    override fun onDestroy() {}

    override fun getCount(): Int {
        return filteredTasks.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_item_task)
        val task = filteredTasks[position]

        views.setTextViewText(R.id.descripcionTarea, task.getDescription())

        val days = task.getDaysLeft()
        if (days == -10000) {
            views.setViewVisibility(R.id.fechaLimiteTarea, View.INVISIBLE)
        } else {
            views.setViewVisibility(R.id.fechaLimiteTarea, View.VISIBLE)
            views.setTextViewText(R.id.fechaLimiteTarea, "Dias restantes: $days.")
            if (days < 0) {
                views.setTextColor(R.id.fechaLimiteTarea, Color.RED)
            }
        }

        if (task.getPrice().compareTo(BigDecimal(0)) == 0) {
            views.setViewVisibility(R.id.recompensaTarea, View.INVISIBLE)
        } else {
            views.setViewVisibility(R.id.recompensaTarea, View.VISIBLE)
            views.setTextViewText(R.id.recompensaTarea, "${task.getPrice()}")
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun filterTasks(taskType: String, tasks: MutableList<Task>) {
        filteredTasks.clear()
        when (taskType) {
            Constants.PENDIENTES -> filteredTasks.addAll(tasks.filter { it.getState() == TaskState.OPEN })
            Constants.COMPLETADAS -> filteredTasks.addAll(tasks.filter { it.getState() == TaskState.COMPLETE })
            Constants.OBLIGATORIAS -> filterMandatory(tasks)
            Constants.EXTRA -> filterNoMandatory(tasks)
            else -> {}
        }
    }

    private fun getTasks() {
        var tasks = mutableListOf<Task>()
        if (ContextFamily.family != null) {
            tasks = ContextFamily.family!!.getTaskList()
        }
        filterTasks(taskType, tasks)
    }

    private fun filterMandatory(tasks: MutableList<Task>) {
        filteredTasks.clear()
        for (task in tasks) {
            if (task.isMandatory() && task.getState() == TaskState.OPEN) {
                filteredTasks.add(task)
            }
        }
    }

    private fun filterNoMandatory(tasks: MutableList<Task>) {
        filteredTasks.clear()
        for (task in tasks) {
            if (!task.isMandatory() && task.getState() == TaskState.OPEN) {
                filteredTasks.add(task)
            }
        }
    }
}
