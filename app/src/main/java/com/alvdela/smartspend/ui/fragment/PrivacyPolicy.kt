package com.alvdela.smartspend.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ScrollView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.ui.activity.LoginActivity
import com.alvdela.smartspend.ui.activity.SignInActivity

class PrivacyPolicy : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
        val fragment = view.findViewById<ScrollView>(R.id.fragmentPrivacyPolicy)
        closeButton.setOnClickListener {
            if (LoginActivity.isPrivacyPolicyShown) {
                LoginActivity.hidePrivacyTerms()
                LoginActivity.isPrivacyPolicyShown = false
            }
            if (SignInActivity.isPrivacyPolicyShown) {
                SignInActivity.hidePrivacyTerms()
                SignInActivity.isPrivacyPolicyShown = false

            }
        }
    }
}