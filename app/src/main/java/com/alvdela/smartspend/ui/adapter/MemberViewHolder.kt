package com.alvdela.smartspend.ui.adapter

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Member
import com.alvdela.smartspend.model.Parent

class MemberViewHolder(val view: View) : ViewHolder(view) {

    val ivMemberPicture = view.findViewById<ImageView>(R.id.ivMemberPicture)
    val tvMemberName = view.findViewById<TextView>(R.id.tvMemberName)
    val modifyMemberButton = view.findViewById<ImageButton>(R.id.modifyMemberButton)
    val deleteMemberButton = view.findViewById<ImageButton>(R.id.deleteMemberButton)
    val asignacionesContainer = view.findViewById<ConstraintLayout>(R.id.asignacionesContainer)
    val line = view.findViewById<View>(R.id.line8)
    val rvAsignaciones = view.findViewById<RecyclerView>(R.id.rvAsignaciones)
    val addAsignacionButton = view.findViewById<TextView>(R.id.addAsignacionButton)

    fun render(
        member: Member,
        editMember: (String) -> Unit,
        deleteMember: (String) -> Unit,
        addAsignacion: (String) -> Unit,
        editAllowance: (Int, String) -> Unit,
        deleteAllowance: (Int, String) -> Unit,
        context: Context
    ) {

        tvMemberName.text = member.getUser()
        if (member is Parent) {
            asignacionesContainer.visibility = View.GONE
            line.visibility = View.GONE
        } else if (member is Child) {
            asignacionesContainer.visibility = View.VISIBLE
            line.visibility = View.VISIBLE
            addAsignacionButton.setOnClickListener { addAsignacion(member.getUser()) }
            val adapter = AllowanceAdapter(
                member.getUser(),
                member.getAllowances(),
                editAllowance,
                deleteAllowance
            )
            rvAsignaciones.layoutManager = LinearLayoutManager(context)
            rvAsignaciones.adapter = adapter
        }
        modifyMemberButton.setOnClickListener { editMember(member.getUser()) }
        deleteMemberButton.setOnClickListener { deleteMember(member.getUser()) }


    }
}