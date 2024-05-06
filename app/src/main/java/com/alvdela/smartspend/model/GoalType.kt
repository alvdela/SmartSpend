package com.alvdela.smartspend.model

enum class GoalType {
    TOYS, ACTIVITIES, GIFT, BOOK;
    companion object {
        fun fromString(text: String): GoalType? {
            return when (text.lowercase()) {
                "toys" -> TOYS
                "activities" -> ACTIVITIES
                "gift" -> GIFT
                "book" -> BOOK
                else -> null
            }
        }

        fun toString(goalType: GoalType): String {
            return when (goalType) {
                TOYS -> "Toys"
                ACTIVITIES -> "Activities"
                GIFT -> "Gift"
                BOOK -> "Book"
            }
        }
    }
}