package com.alvdela.smartspend.firebase

import java.time.format.DateTimeFormatter

object Constants {
    const val FAMILY = "family"

    const val MEMBERS = "members"
    const val TASKS = "tasks"
    const val HISTORIC = "oldTasks"

    const val ALLOWANCES = "allowances"
    const val CASHFLOW = "cashFlow"
    const val GOALS = "goals"

    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

}