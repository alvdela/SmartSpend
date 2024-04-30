package com.alvdela.smartspend.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alvdela.smartspend.R
import com.alvdela.smartspend.Utility
import com.alvdela.smartspend.model.GoalType
import com.alvdela.smartspend.model.SavingGoal


class GoalViewHolder(val view: View): ViewHolder(view) {

    val imageGoal = view.findViewById<ImageView>(R.id.imageGoal)
    val progressGoal = view.findViewById<ProgressBar>(R.id.progressGoal)
    val tvProgreso = view.findViewById<TextView>(R.id.tvProgreso)
    val tvPercentage = view.findViewById<TextView>(R.id.tvPercentage)
    val tvGoalName = view.findViewById<TextView>(R.id.tvGoalName)
    val btExtractMoney = view.findViewById<ImageView>(R.id.btExtractMoney)
    val btSaveMoney = view.findViewById<ImageView>(R.id.btSaveMoney)
    fun render(goal: SavingGoal, saveMoney: (Int) -> Unit, extractTask: (Int) -> Unit, position: Int) {
        when(goal.getType()){
            GoalType.TOYS -> imageGoal.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.goal_toys))
            GoalType.ACTIVITIES -> imageGoal.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.goal_activity))
            GoalType.GIFT -> imageGoal.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.goal_gift))
            GoalType.BOOK -> imageGoal.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.goal_book))
        }

        progressGoal.max = goal.getGoal().toInt()
        progressGoal.progress = goal.getSaving().toInt()

        tvProgreso.text = "${goal.getSaving()} de ${goal.getGoal()}"

        tvPercentage.text = "${Utility.getPercentage(goal.getSaving(),goal.getGoal())}%"

        tvGoalName.text = goal.getDescription()

        btSaveMoney.setOnClickListener { saveMoney(position) }

        btExtractMoney.setOnClickListener { extractTask(position) }
    }

}