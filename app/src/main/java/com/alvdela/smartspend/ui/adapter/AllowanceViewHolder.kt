package com.alvdela.smartspend.ui.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Allowance
import com.alvdela.smartspend.model.AllowanceType

class AllowanceViewHolder(val view: View): RecyclerView.ViewHolder(view) {

    val tvAllowanceName = view.findViewById<TextView>(R.id.tvAllowanceName)
    val tvAllowanceType = view. findViewById<TextView>(R.id.tvAllowanceType)
    val deleteAllowanceButton = view.findViewById<ImageButton>(R.id.deleteAllowanceButton)
    val modifyAllowanceButton = view.findViewById<ImageButton>(R.id.modifyAllowanceButton)

    fun render(
        allowance: Allowance,
        selectedChild: String,
        editAllowance: (Int, String) -> Unit,
        deleteAllowance: (Int, String) -> Unit
    ) {
        tvAllowanceName.text = allowance.getName()
        val money = allowance.getAmount()
        val type = allowance.getType()
        when(type){
            AllowanceType.PUNTUAL -> tvAllowanceType.text = "$money€"
            AllowanceType.SEMANAL -> tvAllowanceType.text = "$money€ X Semana"
            AllowanceType.MENSUAL -> tvAllowanceType.text = "$money€ X Mes"
            AllowanceType.TRIMESTRAL -> tvAllowanceType.text = "$money€ X Trimestre"
            AllowanceType.SEMESTRAL -> tvAllowanceType.text = "$money€ X Semestre"
            AllowanceType.ANUAL -> tvAllowanceType.text = "$money€ X Año"
        }
        modifyAllowanceButton.setOnClickListener { editAllowance(adapterPosition,selectedChild) }
        deleteAllowanceButton.setOnClickListener { deleteAllowance(adapterPosition,selectedChild) }
    }
}