package com.alvdela.smartspend

import com.alvdela.smartspend.model.Family

object ContextFamily {

    var mockFamily: Family? = null
    var familyEmail: String? = null

    var isMock = false

    var record = mutableListOf("Gasto")

    fun reset() {
        mockFamily = null
        familyEmail = null
        isMock = false
    }

    fun addRecord(newString: String){
        var existe = false
        for (i in record){
            if (newString == i) existe = true
        }
        if (!existe){
            record.add(0,newString)
        }
        if (record.size > 3){
            record.removeAt(record.size - 1)
        }
    }
}