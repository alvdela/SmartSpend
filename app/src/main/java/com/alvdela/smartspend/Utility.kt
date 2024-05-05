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
        if (((saving/goal)*100).toString().length > 5){
            return ((saving/goal)*100).toString().substring(0,6)
        }
        return ((saving/goal)*100).toString()
    }

    fun getNaturalNumber(number: Float): Int{
        val numeroString = number.toString()
        val partes = numeroString.split(".")
        return partes[0].toInt()
    }

    fun getDecimalNumber(number: Float): Int{
        val numeroString = number.toString()
        val partes = numeroString.split(".").toMutableList()
        if (partes[1].length > 2){
            partes[1] = partes[1].subSequence(0,2).toString()
        }else if(partes[1].length < 2){
            partes[1] = partes[1] + "0"
        }
        return partes[1].toInt()
    }

    fun formFloatNumber(number: Int, decimal: Int): Float{
        val str = "$number.${if(decimal < 10){"0$decimal"} else {"$decimal"}}"
        return str.toFloat()
    }
}