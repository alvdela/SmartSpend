package com.alvdela.smartspend

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alvdela.smartspend.domain.Allowance
import com.alvdela.smartspend.domain.AllowanceType
import com.alvdela.smartspend.domain.CashFlow
import com.alvdela.smartspend.domain.CashFlowType
import com.alvdela.smartspend.domain.Child
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.domain.Parent
import java.time.LocalDate


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
        initShowButtons()
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

        signInButton.setOnClickListener {
            goSignIn()
        }

        accessButton.setOnClickListener {
            //TODO acceso mediante firebase
        }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goProfiles() {
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun createMockFamily() {
        family = Family("Invitados", "email@email.com")
        val parent = Parent("Invitado", "1234")
        family.addMember(parent)
        val child = Child("Hijo/a")
        val expense1 = CashFlow("Gasto1", 5.5F, CashFlowType.COMIDA, LocalDate.now().minusDays(5))
        val expense2 = CashFlow("Gasto2", 10F, CashFlowType.COMPRAS, LocalDate.now())
        val expense3 = CashFlow("Gasto3", 7.75F, CashFlowType.OCIO, LocalDate.now())
        child.setActualMoney(100f)
        child.addExpense(expense1)
        child.addExpense(expense2)
        child.addExpense(expense3)
        val allowance = Allowance("Propina", LocalDate.now().plusDays(5),5f,AllowanceType.SEMANAL)
        child.addAllowance(allowance)
        family.addMember(child)
        println(child.getCashFlow().toList())
        ContextFamily.mockFamily = family
        ContextFamily.isMock = true
    }


    fun showPrivacyTerms(view: View) {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //TODO mensaje que pregunte al usuario si desea cerrar la aplicacion
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun initShowButtons() {
        val passwordButton = findViewById<CheckBox>(R.id.show_password)

        passwordButton.setOnCheckedChangeListener{ _, isChecked ->
            passwordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
    }
}