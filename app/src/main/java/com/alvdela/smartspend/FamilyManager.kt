package com.alvdela.smartspend

import com.alvdela.smartspend.model.Family
import com.google.firebase.auth.FirebaseAuth

object FamilyManager {

    var family: Family? = null
    var isMock = false

    fun reset() {
        family = null
        FirebaseAuth.getInstance().signOut()
    }
}