package com.alvdela.smartspend.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.alvdela.smartspend.R
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.util.Constants.EXTRA
import com.alvdela.smartspend.util.Constants.OBLIGATORIAS
import com.alvdela.smartspend.util.Constants.WIDGET_TASK

/**
 * Implementation of App Widget functionality.
 */
class TaskChildWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.task_child_widget)

            val intentObligatorias = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(WIDGET_TASK, OBLIGATORIAS)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.listViewObligatorias, intentObligatorias)

            val intentExtra = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(WIDGET_TASK, EXTRA)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.listViewExtra, intentExtra)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Funcionalidad relevante cuando se crea el primer widget
    }

    override fun onDisabled(context: Context) {
        // Funcionalidad relevante cuando se desactiva el último widget
    }
}
