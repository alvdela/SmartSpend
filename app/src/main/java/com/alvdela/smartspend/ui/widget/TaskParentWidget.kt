package com.alvdela.smartspend.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.alvdela.smartspend.R
import com.alvdela.smartspend.ui.activity.LoginActivity
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.util.Constants.COMPLETADAS
import com.alvdela.smartspend.util.Constants.PENDIENTES
import com.alvdela.smartspend.util.Constants.WIDGET_TASK

/**
 * Implementation of App Widget functionality.
 */
class TaskParentWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.task_parent_widget)

            val intentPendientes = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(WIDGET_TASK, PENDIENTES)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.listViewPendientes, intentPendientes)

            val intentCompletadas = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(WIDGET_TASK, COMPLETADAS)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.listViewCompletadas, intentCompletadas)

            val iLogin: PendingIntent = Intent(context, LoginActivity::class.java).let {
                PendingIntent.getActivity(context, 0, it,PendingIntent.FLAG_IMMUTABLE)
            }
            views.apply{setOnClickPendingIntent(R.id.btAddTask,iLogin)}

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewPendientes)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewCompletadas)
        }
    }

    override fun onEnabled(context: Context) {
        // Funcionalidad relevante cuando se crea el primer widget
    }

    override fun onDisabled(context: Context) {
        // Funcionalidad relevante cuando se desactiva el último widget
    }
}