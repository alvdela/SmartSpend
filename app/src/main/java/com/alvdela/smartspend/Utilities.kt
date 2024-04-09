package com.alvdela.smartspend

import java.util.regex.Pattern

object Utilities {

    fun validateEmail(email: String): Boolean {
        val pattern = Pattern.compile("^[\\w\\-\\_\\+]+(\\.[\\w\\-\\_]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$")
        val matcher = pattern.matcher(email)
        return matcher.find()
    }

}