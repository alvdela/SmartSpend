package com.alvdela.smartspend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.domain.Member

class MemberAdapter(private val memberMap: MutableMap<String,Member>,
                    private val editMember: (String) -> Unit,
                    private val deleteMember: (String) -> Unit,
                    private val addAllowance: (String) -> Unit,
                    private val editAllowance: (Int, String) -> Unit,
                    private val deleteAllowance: (Int, String) -> Unit,
                    private val context: Context): RecyclerView.Adapter<MemberViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MemberViewHolder(layoutInflater.inflate(R.layout.item_members, parent, false))
    }

    override fun getItemCount(): Int = memberMap.size

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val key = memberMap.keys.toList()[position]
        val member = memberMap[key]
        if (member != null) {
            holder.render(member,editMember,deleteMember,addAllowance,editAllowance,deleteAllowance, context)
        }
    }


}