package com.alvdela.smartspend.ui

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import java.lang.Thread.sleep


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
    private fun rotateImage(view: View) {
        val animation = RotateAnimation(
            180f, 0f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        animation.setDuration(200)
        view.startAnimation(animation)
    }

    fun spinImage(view: View){
        val animation = RotateAnimation(
            0f, 360f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        animation.setDuration(2000)
        animation.setRepeatCount(Animation.INFINITE)
        animation.repeatMode = Animation.INFINITE
        view.startAnimation(animation)
    }

    fun compactView(button: ImageView, container: RelativeLayout, recycler: RecyclerView) {
        rotateImage(button)
        sleep(300)
        button.setImageResource(R.drawable.ic_compact)
        animateViewOfFloat(recycler, "translationY", -1000f, 200)
        container.visibility = View.GONE
    }

    fun extendView(button: ImageView, container: RelativeLayout, recycler: RecyclerView) {
        rotateImage(button)
        sleep(300)
        button.setImageResource(R.drawable.ic_extend)
        container.visibility = View.VISIBLE
        animateViewOfFloat(recycler, "translationY", 0f, 200)
    }
}