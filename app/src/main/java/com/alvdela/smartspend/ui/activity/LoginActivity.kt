package com.alvdela.smartspend.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.firebase.Constants
import com.alvdela.smartspend.firebase.Constants.ALLOWANCES
import com.alvdela.smartspend.firebase.Constants.CASHFLOW
import com.alvdela.smartspend.firebase.Constants.FAMILY
import com.alvdela.smartspend.firebase.Constants.GOALS
import com.alvdela.smartspend.firebase.Constants.MEMBERS
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates


class LoginActivity : AppCompatActivity() {

    private var email = "mock"
    private var password by Delegates.notNull<String>()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var forgetButton: TextView
    private lateinit var accessButton: TextView
    private lateinit var signInButton: TextView
    private lateinit var mockButton: Button

    private lateinit var mAuth: FirebaseAuth
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        screenSplash.setKeepOnScreenCondition{
            false
        }
        initObjects()
        initShowButtons()
    }

    override fun onStart() {
        super.onStart()
        //TODO autologin
        /*val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            getFamily()
            goProfiles()
        }*/
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
            createMockFamily()
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
                    ContextFamily.isMock = false
                    getFamily()
                }else{
                    errorText.visibility = View.VISIBLE
                }
            }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goProfiles() {
        ContextFamily.familyEmail = email
        if (!ContextFamily.isMock){
            ContextFamily.family!!.setPassword(password)
        }
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun createMockFamily() {
        ContextFamily.isMock = true
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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_LICENSE))
        startActivity(intent)
    }

    private fun showLoading(){
        val loadingView = findViewById<ConstraintLayout>(R.id.loadingView)
        loadingView.visibility = View.VISIBLE
        val loadingImage = findViewById<ImageView>(R.id.loadingImage)
        Animations.girarImagen(loadingImage)
    }

    private fun dismissLoading(){
        val loadingView = findViewById<ConstraintLayout>(R.id.loadingView)
        loadingView.visibility = View.GONE
    }

    /* Metodos para obtener los datos de firebase */

    private fun getFamily() {
        showLoading()
        FirebaseFirestore.getInstance()
            .collection(email)
            .document(FAMILY)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val familyName = document.getString("familyName")
                    val familyEmail = document.getString("familyEmail")
                    val family = Family(familyName!!, familyEmail!!)
                    ContextFamily.family = family
                    getMembers()
                }else{
                    Toast.makeText(this, "Error con firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error con firebase", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getMembers() {
        FirebaseFirestore.getInstance()
            .collection(email)
            .document(FAMILY)
            .collection(MEMBERS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (MemberType.fromString(document.getString("type")!!) == MemberType.PARENT) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val parent = Parent(user, password)
                        ContextFamily.family?.addMember(parent)
                    } else if (MemberType.fromString(document.getString("type")!!) == MemberType.CHILD) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val money = document.getLong("money")!!.toFloat()
                        val child = Child(user, password)
                        child.setActualMoney(money)
                        ContextFamily.family?.addMember(child)
                    }
                }
                updateChildren()
            }
            .addOnFailureListener { exception ->
                println("Error getting document: $exception")
            }
    }

    private fun updateChildren() {
        for (child in ContextFamily.family!!.getChildren()){
            getAllowances(child)
            child.setCashFlow(getCashFlow(child.getUser()))
            child.setGoals(getGoals(child.getUser()))
        }
        dismissLoading()
        getTask(Constants.TASKS)
        getTask(Constants.HISTORIC)
        goProfiles()
    }

    private fun getAllowances(child: Child) {
        FirebaseFirestore.getInstance()
            .collection(email)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getUser())
            .collection(ALLOWANCES)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name")!!
                    val nextPaymentString = document.getString("nextPayment")!!
                    val nextPayment = LocalDate.parse(nextPaymentString, dateFormat)
                    val amount = document.getDouble("amount")!!.toFloat()
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
            .collection(email)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(CASHFLOW)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val amount = document.getLong("amount")!!.toFloat()
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
        FirebaseFirestore.getInstance().collection(email)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val savingGoal = document.getDouble("goal")!!.toFloat()
                    val saving = document.getDouble("saving")!!.toFloat()
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
        val family = ContextFamily.family!!
        FirebaseFirestore.getInstance()
            .collection(email)
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

                    val price = document.getDouble("price")!!.toFloat()

                    var state = TaskState.OPEN
                    when (document.getString("state")!!) {
                        "OPEN" -> state = TaskState.OPEN
                        "COMPLETE" -> state = TaskState.COMPLETE
                    }

                    val task = Task(description, limitDate, mandatory, price, state)
                    val id = document.id
                    task.setId(id)
                    if (state == TaskState.COMPLETE) {
                        val child = family.getMember(document.getString("child")!!) as Child
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
                        family.addTaskToHistoric(task)
                    }
                }
            }
    }
}