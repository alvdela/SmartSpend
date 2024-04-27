package com.alvdela.smartspend.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.CashFlow

class ExpenseAdapter(private val cashFlowList: MutableList<CashFlow> = mutableListOf()
) : RecyclerView.Adapter<ExpenseViewHolder>() {

    private var lastDate = cashFlowList[0].date
    private var showDate = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ExpenseViewHolder(layoutInflater.inflate(R.layout.item_expense, parent, false))
    }

    override fun getItemCount(): Int = cashFlowList.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        if (position == 0){
            showDate = true
        }
        if (cashFlowList[position].date != lastDate){
            showDate = true
        }
        val item = cashFlowList[position]
        holder.render(item, showDate)
        showDate = false
    }
}