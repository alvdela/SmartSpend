package com.alvdela.smartspend.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.view.animation.AnimationUtils
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
import com.alvdela.smartspend.util.CropImage.getCroppedBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MemberViewHolder(val view: View) : ViewHolder(view) {

    private val ivMemberPicture = view.findViewById<ImageView>(R.id.ivMemberPicture)
    private val tvMemberName = view.findViewById<TextView>(R.id.tvMemberName)
    private val modifyMemberButton = view.findViewById<ImageButton>(R.id.modifyMemberButton)
    private val deleteMemberButton = view.findViewById<ImageButton>(R.id.deleteMemberButton)
    private val asignacionesContainer = view.findViewById<ConstraintLayout>(R.id.asignacionesContainer)
    private val line = view.findViewById<View>(R.id.line8)
    private val rvAsignaciones = view.findViewById<RecyclerView>(R.id.rvAsignaciones)
    private val addAsignacionButton = view.findViewById<TextView>(R.id.addAsignacionButton)

    fun render(
        member: Member,
        editMember: (String) -> Unit,
        deleteMember: (String) -> Unit,
        addAsignacion: (String) -> Unit,
        editAllowance: (Int, String) -> Unit,
        deleteAllowance: (Int, String) -> Unit,
        context: Context
    ) {

        showProfilePicture(member)

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

        val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.fade_anim)
        itemView.startAnimation(animation)
    }

    private fun showProfilePicture(member: Member) {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid
        val fileName = member.getId()

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$fileName")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                if (localFile.exists() && localFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    if (bitmap != null) {
                        ivMemberPicture.scaleType = ImageView.ScaleType.FIT_CENTER
                        ivMemberPicture.setImageBitmap(getCroppedBitmap(bitmap))
                    }
                }
            }
            .addOnFailureListener{
                ivMemberPicture.setColorFilter(Color.BLACK)
            }
    }
}