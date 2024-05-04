package com.alvdela.smartspend.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.firebase.FirebaseManager
import com.alvdela.smartspend.model.Allowance
import com.alvdela.smartspend.model.AllowanceType
import com.alvdela.smartspend.model.CashFlow
import com.alvdela.smartspend.model.CashFlowType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.ui.Animations
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class LoginActivity : AppCompatActivity() {

    private var email = "email@email.com"
    private var password by Delegates.notNull<String>()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var forgetButton: TextView
    private lateinit var accessButton: TextView
    private lateinit var signInButton: TextView
    private lateinit var mockButton: Button

    private lateinit var mAuth: FirebaseAuth

    companion object{
        var isPrivacyPolicyShown = false
        private lateinit var fragmentContainer: FragmentContainerView
        const val URL_LICENSE = "https://creativecommons.org/licenses/by-nc/4.0/"
        fun hidePrivacyTerms(){
            Animations.animateViewOfFloat(fragmentContainer,"translationY", 3000f,300)
            isPrivacyPolicyShown = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)
        initObjects()
        initShowButtons()
    }

    private fun initObjects() {
        emailInput = findViewById(R.id.familia_login)
        passwordInput = findViewById(R.id.password_login)

        errorText = findViewById(R.id.error_text)

        forgetButton = findViewById(R.id.forget_button)

        accessButton = findViewById(R.id.acceder_button)
        signInButton = findViewById(R.id.registrarse_button)
        mockButton = findViewById(R.id.mock_button)

        errorText.visibility = View.INVISIBLE

        mockButton.setOnClickListener {
            ContextFamily.isMock = true
            val family = createMockFamily()
            if (family != null) goProfiles(family, email)
            else Toast.makeText(this, "Error al crear la familia de muestra", Toast.LENGTH_SHORT).show()
        }

        signInButton.setOnClickListener {
            goSignIn()
        }

        accessButton.setOnClickListener {
            loginUser()
        }

        fragmentContainer = findViewById(R.id.fragmentPrivacy)
    }

    private fun loginUser() {
        email = emailInput.text.toString()
        password = passwordInput.text.toString()

        mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    //Todo getDataFromFireBase
                    val family = Family("a",email)
                    goProfiles(family,email)
                }else{
                    errorText.visibility = View.VISIBLE
                }
            }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goProfiles(family: Family, email: String) {
        ContextFamily.family = family
        ContextFamily.familyEmail = email
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun createMockFamily(): Family? {
        val family = FirebaseManager.getInstance().getFamily("mock")
        sleep(1000)
        if (family != null){
            runBlocking {
                FirebaseManager.getInstance().getMembers(family)
            }
        }

        return family
    }

    fun showPrivacyTerms(view: View) {
        Animations.animateViewOfFloat(fragmentContainer,"translationY", 0f,300)
        isPrivacyPolicyShown = true
    }

    override fun onBackPressed() {
        if (isPrivacyPolicyShown){
            hidePrivacyTerms()
        }else{
            super.onBackPressed()
            //TODO mensaje que pregunte al usuario si desea cerrar la aplicacion
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }
    }

    private fun initShowButtons() {
        val passwordButton = findViewById<CheckBox>(R.id.show_password)

        passwordButton.setOnCheckedChangeListener{ _, isChecked ->
            passwordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
    }

    fun showLicense(view: View) {
        /*val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_LICENSE))
        startActivity(intent)*/
    }
}