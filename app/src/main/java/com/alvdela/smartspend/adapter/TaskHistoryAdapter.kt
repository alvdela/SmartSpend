package com.alvdela.smartspend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import java.time.LocalDate

class TaskHistoryAdapter(
    private val tasksHistory: MutableList<Task>,
    private val reOpenTask: (Int) -> Unit
) : RecyclerView.Adapter<TaskHistoryViewHolder>() {

    private lateinit var lastDate: LocalDate
    private var showDate = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskHistoryViewHolder(layoutInflater.inflate(R.layout.item_task_history, parent, false))
    }

    override fun getItemCount(): Int = tasksHistory.size

    override fun onBindViewHolder(holder: TaskHistoryViewHolder, position: Int) {
        if (position == 0){
            lastDate = tasksHistory[position].getCompletedDate()!!
            showDate = true
        }
        if (tasksHistory[position].getCompletedDate()!! != lastDate){
            showDate = true
            lastDate = tasksHistory[position].getCompletedDate()!!
        }
        val task = tasksHistory[position]
        holder.render(task,reOpenTask,position, showDate)
        showDate = false
    }

}