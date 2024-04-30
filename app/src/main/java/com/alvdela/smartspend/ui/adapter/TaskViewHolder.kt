package com.alvdela.smartspend.ui.adapter

import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import java.lang.Thread.sleep

class TaskViewHolder(val view: View): ViewHolder(view) {

    val checkTask = view.findViewById<ToggleButton>(R.id.checkTask)
    val descripcionTarea = view.findViewById<TextView>(R.id.descripcionTarea)
    val recompensaTarea = view.findViewById<TextView>(R.id.recompensaTarea)
    val fechaLimiteTarea = view.findViewById<TextView>(R.id.fechaLimiteTarea)
    fun render(task: Task, completeTask: (Int) -> Unit, position: Int) {
        checkTask.isChecked = true
        if (task.getPrice() == 0f){
            recompensaTarea.visibility = View.GONE
        }else{
            recompensaTarea.visibility = View.VISIBLE
            recompensaTarea.text = "${task.getPrice()}€"
        }
        descripcionTarea.text = task.getDescription()
        val days = task.getDaysLeft()
        if (days == -10000){
            fechaLimiteTarea.visibility = View.GONE
        }else{
            fechaLimiteTarea.visibility = View.VISIBLE
            fechaLimiteTarea.text = "Dias restantes: $days"
            if(days <= 0) {
                fechaLimiteTarea.setTextColor(Color.RED)
            }
        }
        checkTask.setOnCheckedChangeListener { _, _ ->
            checkTask.isChecked = false
            completeTask(position)
        }
        val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.fade_anim)
        itemView.startAnimation(animation)
    }

}