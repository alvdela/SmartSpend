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

    private var currentId = 0

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
        val intent = Intent(this, ProfilesActivity::class.java)
        startActivity(intent)
    }

    private fun createMockFamily() {
        val parent = Parent("Invitado", "")
        family = Family("Invitados", "email@email.com")
        family.addMember(getNextId(),parent)
        ContextFamily.mockFamily = family
    }


    fun showPrivacyTerms(view: View) {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun getNextId(): Int{
        return currentId++
    }
}