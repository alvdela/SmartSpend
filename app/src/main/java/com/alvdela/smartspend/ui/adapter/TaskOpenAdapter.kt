package com.alvdela.smartspend.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState

class TaskOpenAdapter(
    private val tasks: MutableList<Task> = mutableListOf(),
    private val completeTask: (Int) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    private val filteredTasks: MutableList<Task> = mutableListOf()

    init {
        filterTasks()
    }

    fun filterTasks() {
        filteredTasks.clear()
        for (task in tasks) {
            if (task.getState() == TaskState.OPEN) {
                filteredTasks.add(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.item_task, parent, false))
    }

    override fun getItemCount(): Int = filteredTasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTasks[position]
        holder.render(task, completeTask, tasks.indexOf(task))
    }

    fun notifyNewTask(){
        filterTasks()
        notifyItemInserted(filteredTasks.size)
    }

    fun removeItem() {
        filterTasks()
        notifyDataSetChanged()
    }
}