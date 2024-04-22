package com.alvdela.smartspend.filters

import java.util.regex.Pattern

object EmailFilter {
    fun validateEmail(email: String): Boolean {
        val pattern =
            Pattern.compile("^[\\w\\-\\_\\+]+(\\.[\\w\\-\\_]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$")
        val matcher = pattern.matcher(email)
        return matcher.find()
    }
}