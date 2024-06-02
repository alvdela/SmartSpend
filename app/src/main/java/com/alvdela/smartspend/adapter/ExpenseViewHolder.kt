package com.alvdela.smartspend.adapter

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.CashFlow
import com.alvdela.smartspend.model.CashFlowType
import java.time.format.DateTimeFormatter

class ExpenseViewHolder(val view: View): ViewHolder(view){

    private val divideLine = view.findViewById<View>(R.id.vDivideLine)
    private val tvShowDate = view.findViewById<TextView>(R.id.tvDate)
    private val expenseDescription = view.findViewById<TextView>(R.id.tvExpenseDescription)
    private val expenseAmount = view.findViewById<TextView>(R.id.tvExpenseAmount)
    private val expenseType = view.findViewById<ImageView>(R.id.ivExpenseImage)

    fun render(cashFlowModel: CashFlow, showDate: Boolean){
        if (showDate){
            divideLine.visibility = View.VISIBLE
            tvShowDate.visibility = View.VISIBLE
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            tvShowDate.text = cashFlowModel.date.format(formatter)
        }else{
            divideLine.visibility = View.GONE
            tvShowDate.visibility = View.GONE
        }
        expenseDescription.text = cashFlowModel.description
        if (cashFlowModel.type == CashFlowType.INGRESO || cashFlowModel.type == CashFlowType.RECOMPENSA){
            expenseAmount.setTextColor(ContextCompat.getColor(view.context, R.color.green))
            expenseAmount.text = view.context.getString(R.string.ingreso, cashFlowModel.amount.toString())
        }else{
            expenseAmount.setTextColor(ContextCompat.getColor(view.context, R.color.red))
            expenseAmount.text = view.context.getString(R.string.spent, cashFlowModel.amount.toString())
        }
        when(cashFlowModel.type){
            CashFlowType.COMIDA -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.spent_food))
            CashFlowType.OCIO -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.spent_cinema))
            CashFlowType.COMPRAS -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.spent_shopping))
            CashFlowType.OTROS -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.spent_receipt))
            CashFlowType.INGRESO -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.money))
            CashFlowType.RECOMPENSA -> expenseType.setImageDrawable(ContextCompat.getDrawable(view.context,R.drawable.spent_gift))
        }

    }
}