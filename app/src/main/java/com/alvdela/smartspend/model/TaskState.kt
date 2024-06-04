package com.alvdela.smartspend.model

enum class TaskState {
    OPEN, COMPLETE;
    companion object {
        fun fromString(text: String): TaskState? {
            return when (text.lowercase()) {
                "open" -> OPEN
                "complete" -> COMPLETE
                else -> null
            }
        }

        fun toString(taskState: TaskState): String {
            return when (taskState) {
                OPEN -> "Open"
                COMPLETE -> "Complete"
            }
        }
    }
}