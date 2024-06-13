package com.alvdela.smartspend.ui.activity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentContainerView
import com.alvdela.smartspend.FamilyManager
import com.alvdela.smartspend.R
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.util.Constants.ALLOWANCES
import com.alvdela.smartspend.util.Constants.CASHFLOW
import com.alvdela.smartspend.util.Constants.FAMILY
import com.alvdela.smartspend.util.Constants.GOALS
import com.alvdela.smartspend.util.Constants.MEMBERS
import com.alvdela.smartspend.util.Constants.dateFormat
import com.alvdela.smartspend.model.Allowance
import com.alvdela.smartspend.model.AllowanceType
import com.alvdela.smartspend.model.CashFlow
import com.alvdela.smartspend.model.CashFlowType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.model.GoalType
import com.alvdela.smartspend.model.MemberType
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.model.SavingGoal
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.ui.Animations
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import kotlin.properties.Delegates


class LoginActivity : AppCompatActivity() {

    private var uid = "mock"

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var forgetButton: TextView
    private lateinit var accessButton: TextView
    private lateinit var signInButton: TextView
    private lateinit var mockButton: Button

    private lateinit var dialog: Dialog


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
        val screenSplash: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        screenSplash.setKeepOnScreenCondition{
            false
        }
        initObjects()
        initShowButtons()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            uid = currentUser.uid
            getFamily()
        }
    }

    private fun initObjects() {
        emailInput = findViewById(R.id.familia_login)
        passwordInput = findViewById(R.id.password_login)

        errorText = findViewById(R.id.error_text)

        forgetButton = findViewById(R.id.forgetButton)

        accessButton = findViewById(R.id.acceder_button)
        signInButton = findViewById(R.id.registrarse_button)
        mockButton = findViewById(R.id.mock_button)

        errorText.visibility = View.INVISIBLE

        mockButton.setOnClickListener {
            FamilyManager.isMock = true
            createMockFamily()
        }

        signInButton.setOnClickListener {
            goSignIn()
        }

        accessButton.setOnClickListener {
            loginUser()
        }

        forgetButton.setOnClickListener {
            forgotPassword()
        }

        fragmentContainer = findViewById(R.id.fragmentPrivacy)
    }

    private fun forgotPassword() {
        showPopUp(R.layout.pop_up_recover_password)
        val etRecoverPassword = dialog.findViewById<EditText>(R.id.etRecoverPassword)
        val recoverPasswordButton = dialog.findViewById<Button>(R.id.recoverPasswordButton)
        recoverPasswordButton.setOnClickListener {
            if (!TextUtils.isEmpty(etRecoverPassword.text)){
                FirebaseAuth.getInstance().sendPasswordResetEmail(etRecoverPassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            Toast.makeText(this, "Email enviado a ${etRecoverPassword.text}", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }else{
                            Toast.makeText(this, "No se encontró ninguna cuenta con ese correo", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                Toast.makeText(this, "Por favor indica un email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        email = emailInput.text.toString()
        password = passwordInput.text.toString()
        if (email.isNotBlank() && password.isNotBlank()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified){
                            FamilyManager.isMock = false
                            uid = FirebaseAuth.getInstance().currentUser!!.uid
                            getFamily()
                        }else{
                            Toast.makeText(this, "Por favor, confirme su correo electrónico", Toast.LENGTH_LONG).show()
                        }
                    }else{
                        errorText.visibility = View.VISIBLE
                    }
                }
        }else{
            emailInput.error = "Por favor introduzca un email"
            passwordInput.error = "Por favor introduzca la contraseña"
        }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goProfiles() {
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun createMockFamily() {
        FamilyManager.isMock = true
        FirebaseAuth.getInstance().signInAnonymously()
        uid = "mock"
        getFamily()
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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_LICENSE))
        startActivity(intent)
    }

    private fun showLoading(){
        val loadingView = findViewById<ConstraintLayout>(R.id.loadingView)
        loadingView.visibility = View.VISIBLE
        val loadingImage = findViewById<ImageView>(R.id.loadingImage)
        Animations.spinImage(loadingImage)
    }

    private fun dismissLoading(){
        val loadingView = findViewById<ConstraintLayout>(R.id.loadingView)
        loadingView.visibility = View.GONE
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    /* Metodos para obtener los datos de firebase */

    private fun getFamily() {
        showLoading()
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val familyName = document.getString("familyName")
                    var familyEmail = document.getString("familyEmail")
                    if (!FamilyManager.isMock){
                        val user = FirebaseAuth.getInstance().currentUser
                        if (familyEmail != user?.email){
                            familyEmail = user?.email
                            updateEmail(familyEmail)
                        }
                    }
                    val family = Family(familyName!!, familyEmail!!)
                    FamilyManager.family = family
                    getMembers()
                }else{
                    Toast.makeText(this, "Error con firebase", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    dismissLoading()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error con firebase", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                dismissLoading()
            }
    }

    private fun updateEmail(familyEmail: String?) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .update(
                mapOf(
                    "familyEmail" to familyEmail
                )
            )
    }

    private fun getMembers() {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (MemberType.fromString(document.getString("type")!!) == MemberType.PARENT) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val parent = Parent(user, password)
                        val id = document.id
                        parent.setId(id)
                        FamilyManager.family?.addMember(parent)
                    } else if (MemberType.fromString(document.getString("type")!!) == MemberType.CHILD) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val money = document.getString("money")!!.toBigDecimal()
                        val child = Child(user, password)
                        val id = document.id
                        child.setId(id)
                        child.setActualMoney(money)
                        FamilyManager.family?.addMember(child)
                    }
                }
                getChildrenInfo()
            }
            .addOnFailureListener { exception ->
                println("Error getting document: $exception")
            }
    }

    private fun getChildrenInfo() {
        for (child in FamilyManager.family!!.getChildren()){
            getAllowances(child)
            child.setCashFlow(getCashFlow(child.getId()))
            child.setGoals(getGoals(child.getId()))
        }
        dismissLoading()
        getTask(Constants.TASKS)
        getTask(Constants.HISTORIC)
        goProfiles()
    }

    private fun getAllowances(child: Child) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getId())
            .collection(ALLOWANCES)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name")!!
                    val nextPaymentString = document.getString("nextPayment")!!
                    val nextPayment = LocalDate.parse(nextPaymentString, dateFormat)
                    val amount = document.getString("amount")!!.toBigDecimal()
                    var type = AllowanceType.fromString(document.getString("type")!!)
                    if (type == null) {
                        type = AllowanceType.PUNTUAL
                    }
                    val id = document.id
                    val allowance = Allowance(name, nextPayment, amount, type)
                    allowance.setId(id)
                    child.addAllowance(allowance)
                }
            }
    }

    private fun getCashFlow(child: String): MutableList<CashFlow> {
        val cashFlowList = mutableListOf<CashFlow>()
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(CASHFLOW)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val amount = document.getString("amount")!!.toBigDecimal()
                    val type = CashFlowType.fromString(document.getString("type")!!)
                    val date = LocalDate.parse(document.getString("date")!!, dateFormat)
                    val cashFlow = CashFlow(description, amount, type!!, date)
                    var index = 0
                    while (index < cashFlowList.size && cashFlowList[index].date.isAfter(cashFlow.date)) {
                        index++
                    }
                    cashFlowList.add(index, cashFlow)
                }
            }
        return cashFlowList
    }

    private fun getGoals(child: String): MutableList<SavingGoal> {
        val goals = mutableListOf<SavingGoal>()
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val savingGoal = document.getString("goal")!!.toBigDecimal()
                    val saving = document.getString("saving")!!.toBigDecimal()
                    val type = GoalType.fromString(document.getString("type")!!)
                    val id = document.id

                    val goal = SavingGoal(description, savingGoal, type!!)
                    goal.saveMoney(saving)
                    goal.setId(id)

                    goals.add(goal)
                }
            }
        return goals
    }

    private fun getTask(typeOfTask: String) {
        val family = FamilyManager.family!!
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(typeOfTask)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!

                    val limitDateString = document.getString("limitDate")!!
                    var limitDate: LocalDate? = null
                    if (limitDateString != "") {
                        limitDate = LocalDate.parse(limitDateString, dateFormat)
                    }

                    val mandatory = document.getBoolean("mandatory")!!

                    val price = document.getString("price")!!.toBigDecimal()

                    val state: TaskState = TaskState.fromString(document.getString("state")!!)!!

                    val task = Task(description, limitDate, mandatory, price, state)
                    val id = document.id
                    task.setId(id)

                    val assigned = document.getBoolean("assigned")!!
                    task.setAssigned(assigned)

                    if (document.getString("child")!! != ""){
                        val child = family.getMemberById(document.getString("child")!!) as Child
                        task.setChild(child)
                    }

                    if (typeOfTask == Constants.HISTORIC) {
                        val completedDateString = document.getString("completedDate")!!
                        var completedDate: LocalDate?
                        if (completedDateString != "") {
                            completedDate = LocalDate.parse(completedDateString, dateFormat)
                            task.setCompletedDate(completedDate)
                        }
                    }
                    if (typeOfTask == Constants.TASKS){
                        family.addTask(task)
                    }else if (typeOfTask == Constants.HISTORIC){
                        family.addTaskToHistory(task)
                    }
                }
            }
    }
}