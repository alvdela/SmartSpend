package com.alvdela.smartspend.util

import java.time.format.DateTimeFormatter

object Constants {
    const val FAMILY = "family"

    const val MEMBERS = "members"
    const val TASKS = "tasks"
    const val HISTORIC = "oldTasks"

    const val ALLOWANCES = "allowances"
    const val CASHFLOW = "cashFlow"
    const val GOALS = "goals"

    const val PREFERENCES = "preferences"
    const val EMAIL_NOTIFICATIONS = "emailNotifications"
    const val PUSH_NOTIFICATIONS = "pushNotifications"

    const val CHANNEL_ID = "0"

    const val WIDGET_TASK = "TASK_TYPE"
    const val PENDIENTES = "PENDIENTES"
    const val COMPLETADAS = "COMPLETADAS"
    const val OBLIGATORIAS = "OBLIGATORIAS"
    const val EXTRA = "EXTRA"

    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

}