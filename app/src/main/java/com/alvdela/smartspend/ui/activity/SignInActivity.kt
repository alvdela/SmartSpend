package com.alvdela.smartspend.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alvdela.smartspend.R

class SignInActivity : AppCompatActivity() {

    private lateinit var familyNameInput: EditText
    private lateinit var tutorNameInput: EditText
    private lateinit var warningName: TextView
    private lateinit var emailInput: EditText
    private lateinit var emailWarning: TextView
    private lateinit var passwordInput: EditText
    private lateinit var passwordWarning: TextView
    private lateinit var passwordAgainInput: EditText
    private lateinit var passwordWarning2: TextView
    private lateinit var checkTerms: CheckBox
    private lateinit var registrarseButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initObjects()
    }

    private fun initObjects() {
        familyNameInput = findViewById(R.id.familia_nickname)
        tutorNameInput = findViewById(R.id.tutor_nickname)
        warningName = findViewById(R.id.tv_advise_name)
        emailInput = findViewById(R.id.email)
        emailWarning = findViewById(R.id.tv_advise_email)
        passwordInput = findViewById(R.id.password)
        passwordWarning = findViewById(R.id.tv_advise_password)
        passwordAgainInput = findViewById(R.id.password_again)
        passwordWarning2 = findViewById(R.id.tv_advise_password2)
        checkTerms = findViewById(R.id.terminos_check)
        registrarseButton = findViewById(R.id.registrarse_button)
    }

    fun showPrivacyTerms(view: View) {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun initShowButtons() {
        val passwordButton = findViewById<CheckBox>(R.id.show_password)
        val repeatButton = findViewById<CheckBox>(R.id.show_repeat)

        passwordButton.setOnCheckedChangeListener{ _, isChecked ->
            passwordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        repeatButton.setOnCheckedChangeListener{ _, isChecked ->
            passwordAgainInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
    }
}