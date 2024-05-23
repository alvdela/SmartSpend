package com.alvdela.smartspend

import com.alvdela.smartspend.model.Family
import com.google.firebase.auth.FirebaseAuth

object ContextFamily {

    var family: Family? = null
    var isMock = false

    fun reset() {
        family = null
        isMock = false
        FirebaseAuth.getInstance().signOut()
    }
}