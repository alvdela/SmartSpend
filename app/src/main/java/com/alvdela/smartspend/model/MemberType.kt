package com.alvdela.smartspend.model

enum class MemberType {
    PARENT, CHILD;
    companion object {
        fun fromString(text: String): MemberType? {
            return when (text.lowercase()) {
                "parent" -> PARENT
                "child" -> CHILD
                else -> null
            }
        }

        fun toString(memberType: MemberType): String {
            return when (memberType) {
                PARENT -> "Parent"
                CHILD -> "Child"
            }
        }
    }
}