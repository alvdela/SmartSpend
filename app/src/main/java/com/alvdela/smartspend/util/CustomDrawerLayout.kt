package com.alvdela.smartspend.util

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class CustomDrawerLayout : DrawerLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isDrawerOpen(GravityCompat.START) || isDrawerOpen(GravityCompat.END)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isDrawerOpen(GravityCompat.START) || isDrawerOpen(GravityCompat.END)) {
            return true
        }
        return super.onTouchEvent(ev)
    }
}
