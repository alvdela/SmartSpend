package com.alvdela.smartspend.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.filters.Validator
import com.alvdela.smartspend.firebase.Constants
import com.alvdela.smartspend.model.MemberType
import com.alvdela.smartspend.ui.Animations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class SignInActivity : AppCompatActivity() {
    companion object {
        var isPrivacyPolicyShown = false
        private lateinit var fragmentContainer: FragmentContainerView
        fun hidePrivacyTerms() {
            Animations.animateViewOfFloat(fragmentContainer, "translationY", 3000f, 300)
            isPrivacyPolicyShown = false
        }
    }

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
    private lateinit var signInButton: TextView

    private var familyName = ""
    private var tutorName = ""
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initObjects()
    }

    private fun initObjects() {
        setInputs()
        setWarnings()

        checkTerms = findViewById(R.id.terminos_check)
        fragmentContainer = findViewById(R.id.fragmentPrivacySignIn)

        signInButton = findViewById(R.id.registrarse_button)
        signInButton.setOnClickListener {
            if (checkAllOk()) {
                getData()
                register()
            }
        }

        initShowButtons()
    }

    private fun getData() {
        familyName = familyNameInput.text.toString()
        tutorName = tutorNameInput.text.toString()
        email = emailInput.text.toString()
    }

    private fun register() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwordInput.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addFamily()
                } else {
                    Toast.makeText(this, "Se ha producido un error inesperado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setWarnings() {
        emailWarning = findViewById(R.id.tv_advise_email)
        emailWarning.visibility = View.INVISIBLE
        warningName = findViewById(R.id.tv_advise_name)
        warningName.visibility = View.INVISIBLE
        passwordWarning = findViewById(R.id.tv_advise_password)
        passwordWarning2 = findViewById(R.id.tv_advise_password2)
        passwordWarning2.visibility = View.INVISIBLE
    }

    private fun setInputs() {
        familyNameInput = findViewById(R.id.familia_nickname)
        familyNameInput.setText("")
        tutorNameInput = findViewById(R.id.tutor_nickname)
        familyNameInput.setText("")
        emailInput = findViewById(R.id.email)
        emailInput.setText("")
        passwordInput = findViewById(R.id.password)
        passwordInput.setText("")
        passwordAgainInput = findViewById(R.id.password_again)
        passwordAgainInput.setText("")
    }

    private fun checkAllOk(): Boolean {
        var allOk = true
        if (familyNameInput.text.isBlank()) {
            allOk = false
            familyNameInput.error = "Es necesario un nombre de familia"
        }
        if (tutorNameInput.text.isBlank()) {
            allOk = false
            tutorNameInput.error = "Es necesario un nombre de tutor"
        }
        if (tutorNameInput.text.toString().length > 12) {
            allOk = false
            warningName.visibility = View.VISIBLE
        }
        if (emailInput.text.isBlank()) {
            allOk = false
            emailInput.error = "Es necesario un email"
        } else if (!Validator.validateEmail(email = emailInput.text.toString())) {
            allOk = false
            emailWarning.visibility = View.VISIBLE
        }
        if (passwordInput.text.isBlank()) {
            allOk = false
            passwordInput.error = "Es necesaria una contraseña"
        } else if (!Validator.validatePassword(passwordInput.text.toString())) {
            allOk = false
            passwordWarning.setTextColor(Color.RED)
        }
        if (passwordInput.text.toString() != passwordAgainInput.text.toString()) {
            allOk = false
            passwordWarning2.visibility = View.VISIBLE
        }
        if (!checkTerms.isChecked) {
            allOk = false
            Toast.makeText(this, "Debe aceptar nuestra politica de privacidad", Toast.LENGTH_SHORT)
                .show()
        }
        return allOk
    }

    fun showPrivacyTerms(view: View) {
        isPrivacyPolicyShown = true
        Animations.animateViewOfFloat(fragmentContainer, "translationY", 0f, 300)
    }

    override fun onBackPressed() {
        if (isPrivacyPolicyShown) {
            hidePrivacyTerms()
            isPrivacyPolicyShown = false
        } else {
            super.onBackPressed()
        }
    }

    private fun addFamily() {
        FirebaseFirestore.getInstance()
            .collection(email)
            .document(Constants.FAMILY)
            .set(
                hashMapOf(
                    "familyName" to familyName,
                    "familyEmail" to email,
                )
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addParent()
                }
            }
    }

    private fun addParent() {
        FirebaseFirestore.getInstance()
            .collection(email)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(tutorName)
            .set(
                hashMapOf(
                    "user" to tutorName,
                    "password" to "",
                    "type" to MemberType.toString(MemberType.PARENT)
                )
            )
            .addOnCompleteListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }
    }

    private fun initShowButtons() {
        val passwordButton = findViewById<CheckBox>(R.id.show_password)
        val repeatButton = findViewById<CheckBox>(R.id.show_repeat)

        passwordButton.setOnCheckedChangeListener { _, isChecked ->
            passwordInput.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        repeatButton.setOnCheckedChangeListener { _, isChecked ->
            passwordAgainInput.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}