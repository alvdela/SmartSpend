package com.alvdela.smartspend.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R

class ProfileFragment : Fragment() {
    private var user: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(USER_BUNDLE)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = user

        val tvNombreFamilia = view.findViewById<TextView>(R.id.tvNombreFamilia)
        tvNombreFamilia.text = ContextFamily.family!!.getName()

        if (ContextFamily.family!!.isParent(user!!)){
            val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
            tvEmail.text = ContextFamily.family!!.getEmail()
        }else{
            val lyEmail = view.findViewById<LinearLayout>(R.id.lyEmailProfile)
            lyEmail.visibility = View.GONE
        }

        initButtons(view)

        val toolbarProfile = view.findViewById<Toolbar>(R.id.toolbar_profile)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbarProfile)
        toolbarProfile.setNavigationOnClickListener {
            // Cerrar el Fragment
            configProfileOpen = false
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    private fun initButtons(view: View){
        val btEditUserName = view.findViewById<ImageView>(R.id.btEditUserName)
        btEditUserName.setOnClickListener {
            Toast.makeText(requireContext(),"Boton del fragment pulsado", Toast.LENGTH_SHORT).show()
        }

        val btEditFamilyName = view.findViewById<ImageView>(R.id.btEditFamilyName)
        btEditFamilyName.setOnClickListener {
            Toast.makeText(requireContext(),"Boton del fragment pulsado", Toast.LENGTH_SHORT).show()
        }

        val btEditEmail = view.findViewById<ImageView>(R.id.btEditEmail)
        btEditEmail.setOnClickListener {
            Toast.makeText(requireContext(),"Boton del fragment pulsado", Toast.LENGTH_SHORT).show()
        }

        val btEditPassword = view.findViewById<ImageView>(R.id.btEditPassword)
        btEditPassword.setOnClickListener {
            Toast.makeText(requireContext(),"Boton del fragment pulsado", Toast.LENGTH_SHORT).show()
        }

        val btDeleteMember = view.findViewById<TextView>(R.id.btDeleteMember)
        btDeleteMember.setOnClickListener {
            Toast.makeText(requireContext(),"Boton del fragment pulsado", Toast.LENGTH_SHORT).show()
        }

        if (!ContextFamily.family!!.isParent(user!!)){
            btEditFamilyName.visibility = View.GONE
            btEditEmail.visibility = View.GONE
            btDeleteMember.visibility = View.GONE
        }
    }

    companion object {

        const val USER_BUNDLE = "user_bundle"
        var configProfileOpen = false

        @JvmStatic
        fun newInstance(user: String) =
            ProfileFragment()
                .apply {
                arguments = Bundle().apply {
                    putString(USER_BUNDLE, user)
                }
            }
    }
}