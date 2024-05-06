package com.alvdela.smartspend.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Allowance

class AllowanceAdapter(
    private val selectedChild: String,
    private val allowanceList: MutableList<Allowance> = mutableListOf(),
    private val editAllowance: (Int, String) -> Unit,
    private val deleteAllowance: (Int, String) -> Unit,
) :
    RecyclerView.Adapter<AllowanceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowanceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AllowanceViewHolder(layoutInflater.inflate(R.layout.item_allowance, parent, false))
    }

    override fun getItemCount(): Int = allowanceList.size

    override fun onBindViewHolder(holder: AllowanceViewHolder, position: Int) {
        holder.render(allowanceList[position],selectedChild, editAllowance, deleteAllowance)
    }
}