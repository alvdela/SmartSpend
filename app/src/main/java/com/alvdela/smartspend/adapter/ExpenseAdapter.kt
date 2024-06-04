package com.alvdela.smartspend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.CashFlow
import java.time.LocalDate

class ExpenseAdapter(private val cashFlowList: MutableList<CashFlow> = mutableListOf()
) : RecyclerView.Adapter<ExpenseViewHolder>() {

    private lateinit var lastDate: LocalDate
    private var showDate = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ExpenseViewHolder(layoutInflater.inflate(R.layout.item_expense, parent, false))
    }

    override fun getItemCount(): Int = cashFlowList.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        if (position == 0){
            lastDate = cashFlowList[position].date
            showDate = true
        }
        if (cashFlowList[position].date != lastDate){
            showDate = true
            lastDate = cashFlowList[position].date
        }
        val item = cashFlowList[position]
        holder.render(item, showDate)
        showDate = false
    }
}