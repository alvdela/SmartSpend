package com.alvdela.smartspend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.SavingGoal

class GoalAdapter(
    private val goals: MutableList<SavingGoal> = mutableListOf(),
    private val saveMoney: (Int) -> Unit,
    private val extractTask: (Int) -> Unit
): RecyclerView.Adapter<GoalViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GoalViewHolder(layoutInflater.inflate(R.layout.item_goal, parent, false))
    }

    override fun getItemCount(): Int = goals.size

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.render(goal,saveMoney,extractTask,position)
    }
}