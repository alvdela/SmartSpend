package com.alvdela.smartspend.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState

class TaskCompleteAdapter(
    private val tasks: MutableList<Task>,
    private val completeTask: (Int) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.item_task, parent, false))
    }

    override fun getItemCount(): Int = getCompleteNumber()

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        if (task.getState() == TaskState.COMPLETE) {
            holder.render(task, completeTask, position)
        }
    }

    private fun getCompleteNumber(): Int {
        var completeTasks = 0
        for (task in tasks) {
            if (task.getState() == TaskState.COMPLETE) {
                completeTasks++
            }
        }
        return completeTasks
    }
}