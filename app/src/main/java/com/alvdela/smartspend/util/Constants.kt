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

    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

}