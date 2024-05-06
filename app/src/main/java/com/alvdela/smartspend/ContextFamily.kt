package com.alvdela.smartspend

import com.alvdela.smartspend.model.Family

object ContextFamily {

    var family: Family? = null
    var familyEmail: String? = null

    var isMock = false

    fun reset() {
        family = null
        familyEmail = null
        isMock = false
    }
}