package com.alvdela.smartspend.ui.adapter

import android.graphics.Color
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class TaskHistoryViewHolder(val view: View): ViewHolder(view) {

    private val descripcionTarea = view.findViewById<TextView>(R.id.descripcionTarea)
    private val recompensaTarea = view.findViewById<TextView>(R.id.recompensaTarea)
    private val fechaLimiteTarea = view.findViewById<TextView>(R.id.fechaLimiteTarea)

    private val restartTask = view.findViewById<ImageView>(R.id.restartTask)

    private val lyHistoryTask = view.findViewById<LinearLayout>(R.id.lyHistoryTask)
    private val tvCompletionDate = view.findViewById<TextView>(R.id.tvCompletionDate)

    fun render(task: Task, reOpenTask: (Int) -> Unit, selectedTask: Int, showDate: Boolean) {

        if (task.getPrice().compareTo(BigDecimal(0)) == 0){
            recompensaTarea.visibility = View.GONE
        }else{
            recompensaTarea.visibility = View.VISIBLE
            recompensaTarea.text = "${task.getPrice()}€"
        }

        descripcionTarea.text = task.getDescription()

        val limitDate = task.getLimitDate()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        if (limitDate != null) {
            fechaLimiteTarea.text = limitDate.format(formatter)
        }

        if (showDate){
            val completedDate = task.getCompletedDate()
            if (completedDate != null){
                tvCompletionDate.text = completedDate.format(formatter)
            }
        }else{
            lyHistoryTask.visibility = View.INVISIBLE
        }

        restartTask.setOnClickListener {
            reOpenTask(selectedTask)
        }

        val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.fade_anim)
        itemView.startAnimation(animation)
    }

}