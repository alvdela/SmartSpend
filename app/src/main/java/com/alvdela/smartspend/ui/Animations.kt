package com.alvdela.smartspend.ui

import android.animation.ObjectAnimator
import android.view.View

object Animations {

    /* FUNCIONES DE ANIMACION Y CAMBIOS DE ATRIBUTOS */
    fun animateViewOfInt(v: View, attr: String, value: Int, time: Long){
        ObjectAnimator.ofInt(v,attr,value).apply {
            duration = time
            start()
        }
    }

    fun animateViewOfFloat(v: View, attr: String, value: Float, time: Long){
        ObjectAnimator.ofFloat(v,attr,value).apply {
            duration = time
            start()
        }
    }

}