package com.alvdela.smartspend

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object Utility {
    fun isValidDate(dateStr: String, dateFormat: DateTimeFormatter): Boolean {
        return try {
            LocalDate.parse(dateStr, dateFormat)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun getPercentage(saving: Float, goal: Float): String {
        return ((saving/goal)*100).toString()
    }
}