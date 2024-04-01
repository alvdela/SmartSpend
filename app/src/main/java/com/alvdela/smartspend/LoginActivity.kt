package com.alvdela.smartspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.domain.Member
import com.alvdela.smartspend.domain.Parent


class LoginActivity : AppCompatActivity() {

    private lateinit var familyInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var forgetButton: TextView
    private lateinit var accessButton: TextView
    private lateinit var signInButton: TextView
    private lateinit var mockButton: Button

    private lateinit var family: Family

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)
        initObjects()
    }

    private fun initObjects() {
        familyInput = findViewById(R.id.familia_login)
        passwordInput = findViewById(R.id.password_login)
        errorText = findViewById(R.id.error_text)
        forgetButton = findViewById(R.id.forget_button)
        accessButton = findViewById(R.id.acceder_button)
        signInButton = findViewById(R.id.registrarse_button)
        mockButton = findViewById(R.id.mock_button)

        errorText.visibility = View.INVISIBLE

        mockButton.setOnClickListener {
            createMockFamily()
            goProfiles()
        }
    }

    private fun goProfiles() {
        TODO("Not yet implemented")
    }

    private fun createMockFamily() {
        val parent = Parent("Invitado", "")
        val members: MutableMap<String, Member> = mutableMapOf()
        members[parent.getUser()] = parent
    }


    fun showPrivacyTerms(view: View) {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }
}