package com.alvdela.smartspend.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alvdela.smartspend.R

class ProfileFragment : Fragment() {
    private var user: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(USER_BUNDLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    companion object {

        private const val USER_BUNDLE = "user_bundle"

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