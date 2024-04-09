package com.alvdela.smartspend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.adapter.ExpenseAdapter
import com.alvdela.smartspend.domain.CashFlow
import com.alvdela.smartspend.domain.CashFlowType
import com.alvdela.smartspend.domain.Child
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.filters.DecimalDigitsInputFilter
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate

class MainChildrenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val MAX_DECIMALS = 2

    private lateinit var drawer: DrawerLayout

    //Botones para cambiar entre pantallas
    private lateinit var expensesButton: ImageView
    private lateinit var taskButton: ImageView
    private lateinit var goalsButton: ImageView
    private lateinit var gameButton: ImageView

    //Distintos layouts
    private lateinit var expensesLayout: ConstraintLayout
    private lateinit var taskLayout: ConstraintLayout
    private lateinit var goalsLayout: ConstraintLayout
    private lateinit var gameLayout: ConstraintLayout
    
    //Botones para añadir elementos
    private lateinit var addSpentButton: Button
    private lateinit var addGoalButton: Button
    
    private var user : String = ""
    private lateinit var family: Family
    private lateinit var child: Child

    private var expenses = true
    private var tareas = false
    private var goals = false
    private var games = false

    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_children)
        user = intent.getStringExtra("USER_NAME").toString()
        getFamily()
        initObjects()
        initToolBar()
        initNavView()
        showMoney()
        showCashFlow()
    }

    private fun initObjects() {
        expensesLayout = findViewById(R.id.expensesLayout)
        addSpentButton = findViewById(R.id.buttonAddSpent)
        taskLayout = findViewById(R.id.tareasLayout)
        goalsLayout = findViewById(R.id.goalsLayout)
        addGoalButton = findViewById(R.id.buttonAddGoal)
        gameLayout = findViewById(R.id.gamesLayout)
        expensesButton = findViewById(R.id.expenses_button)
        taskButton = findViewById(R.id.task_button)
        goalsButton = findViewById(R.id.goals_button)
        gameButton = findViewById(R.id.games_button)

        val currentUserName = findViewById<TextView>(R.id.tvCurrentUserName)
        currentUserName.text = user
        changeButtonState(expensesButton)

        expensesButton.setOnClickListener {
            if (!expenses){
                restartButtons()
                changeButtonState(expensesButton)
                animateExpenses()
            }
        }
        taskButton.setOnClickListener {
            if (!tareas){
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }
        }
        goalsButton.setOnClickListener {
            if (!goals){
                restartButtons()
                changeButtonState(goalsButton)
                animateGoals()
            }
        }
        gameButton.setOnClickListener {
            if (!games){
                restartButtons()
                changeButtonState(gameButton)
                animateGames()
            }
        }

        addSpentButton.setOnClickListener {
            addSpent()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addSpent() {
        val popUp = findViewById<ConstraintLayout>(R.id.popUpAddSpent)
        popUp.visibility = View.VISIBLE

        val descripcion = findViewById<AutoCompleteTextView>(R.id.inputDescripcionGasto)
        descripcion.setText("")
        val autoComplete = CustomSpinnerAdapter(this, ContextFamily.record.toList())
        descripcion.setAdapter(autoComplete)
        descripcion.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descripcion.showDropDown()
        }

        val amount = findViewById<EditText>(R.id.inputCantidadGasto)
        amount.setText("")
        amount.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        var tipo = 0
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            tipo = selectedRadioButton.tag.toString().toInt()
        }

        val closeIcon = findViewById<ImageView>(R.id.closePopUpAddSpent)
        closeIcon.setOnClickListener {
            popUp.visibility = View.GONE
        }

        val cancelButton = findViewById<Button>(R.id.cancelNewSpent)
        cancelButton.setOnClickListener {
            popUp.visibility = View.GONE
        }

        val addButton = findViewById<Button>(R.id.addNewSpent)
        addButton.setOnClickListener {
            val descripcionText = descripcion.text.toString()
            var amountNumber = 0f
            if (descripcionText.isEmpty()){
                descripcion.error = "Se necesita una descripción"
                Toast.makeText(this,"Se necesita una descripción",Toast.LENGTH_SHORT).show()
            }else if(amount.text.toString().isEmpty()){
                amount.error = "La cantidad no puede quedar vacia"
                Toast.makeText(this,"La cantidad no puede quedar vacia",Toast.LENGTH_SHORT).show()
            }else if(amount.text.toString().toFloat() > child.getActualMoney()){
                amount.error = "La cantidad no puede ser mayor que la cantidad disponible"
                Toast.makeText(this,"La cantidad no puede ser mayor que la cantidad disponible",Toast.LENGTH_SHORT).show()
            }else{
                amountNumber = amount.text.toString().toFloat()
                ContextFamily.addRecord(descripcionText)
                when(tipo){
                    1-> {
                        val newExpense = CashFlow(descripcionText,amountNumber,CashFlowType.COMIDA,
                            LocalDate.now())
                        child.addExpense(newExpense)
                    }
                    2-> {
                        val newExpense = CashFlow(descripcionText,amountNumber,CashFlowType.COMPRAS,
                            LocalDate.now())
                        child.addExpense(newExpense)
                    }
                    3-> {
                        val newExpense = CashFlow(descripcionText,amountNumber,CashFlowType.OCIO,
                            LocalDate.now())
                        child.addExpense(newExpense)
                    }
                    else -> {
                        val newExpense = CashFlow(descripcionText,amountNumber,CashFlowType.OTROS,
                            LocalDate.now())
                        child.addExpense(newExpense)
                    }
                }
                adapter.notifyDataSetChanged()
                popUp.visibility = View.GONE
                showMoney()
            }
        }
    }

    private fun animateExpenses() {
        if (tareas){
            Animations.animateViewOfFloat(taskLayout, "translationX", 2000f, 300)
        }
        if (goals){
            Animations.animateViewOfFloat(goalsLayout, "translationX", 2000f, 300)
        }
        if (games){
            Animations.animateViewOfFloat(gameLayout, "translationX", 2000f, 300)
        }
        expensesLayout.translationX = -2000f
        Animations.animateViewOfFloat(expensesLayout, "translationX", 0f, 300)
        expenses = true
        tareas = false
        goals = false
        games = false
    }
    private fun animateTareas() {
        if (expenses){
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
            taskLayout.translationX = 2000f
        }
        if (goals){
            Animations.animateViewOfFloat(goalsLayout, "translationX", 2000f, 300)
            taskLayout.translationX = -2000f
        }
        if (games){
            Animations.animateViewOfFloat(gameLayout, "translationX", 2000f, 300)
            taskLayout.translationX = -2000f
        }
        Animations.animateViewOfFloat(taskLayout, "translationX", 0f, 300)
        expenses = false
        tareas = true
        goals = false
        games = false
    }
    private fun animateGoals() {
        if (expenses){
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
            goalsLayout.translationX = 2000f
        }
        if (tareas){
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
            goalsLayout.translationX = 2000f
        }
        if (games){
            Animations.animateViewOfFloat(gameLayout, "translationX", 2000f, 300)
            goalsLayout.translationX = -2000f
        }
        Animations.animateViewOfFloat(goalsLayout, "translationX", 0f, 300)
        expenses = false
        tareas = false
        goals = true
        games = false
    }
    private fun animateGames() {
        if (expenses){
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
        }
        if (goals){
            Animations.animateViewOfFloat(goalsLayout, "translationX", -2000f, 300)
        }
        if (tareas){
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
        }
        gameLayout.translationX = 2000f
        Animations.animateViewOfFloat(gameLayout, "translationX", 0f, 300)
        expenses = false
        tareas = false
        goals = false
        games = true
    }

    private fun changeButtonState(button: ImageView) {
        button.setBackgroundColor(ContextCompat.getColor(this,R.color.light_blue))
        button.setColorFilter(ContextCompat.getColor(this,R.color.mid_gray))
    }

    private fun restartButtons() {
        expensesButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        taskButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        goalsButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        gameButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))

        expensesButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_gray))
        taskButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_gray))
        goalsButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_gray))
        gameButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_gray))
    }

    private fun getFamily() {
        if (ContextFamily.isMock) {
            family = ContextFamily.mockFamily!!
            child = family.getMember(user) as Child
        } else {
            //TODO consulta a firebase
        }
    }

    private fun initToolBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.main_children)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.bar_tittle,
            R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toggle)

        toggle.syncState()
    }

    private fun initNavView() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val headerView: View =
            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        /*val tvUser: TextView = findViewById(R.id.tvUserEmail)
        tvUser.text = family.getEmail()*/
    }

    @Deprecated("Deprecated")
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val popUpLogOut = findViewById<ConstraintLayout>(R.id.popUpBackProfiles)
            popUpLogOut.visibility = View.VISIBLE
            val cancelButton = findViewById<Button>(R.id.cancelButtonBackProfiles)
            cancelButton.setOnClickListener {
                popUpLogOut.visibility = View.GONE
            }
            val confirmButton = findViewById<Button>(R.id.confirmButtonBackProfiles)
            confirmButton.setOnClickListener {
                popUpLogOut.visibility = View.GONE
                super.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_item_signout -> signOut()
            R.id.nav_item_backprofiles -> backProfiles()

        }

        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun backProfiles() {
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun signOut() {
        //FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        ContextFamily.mockFamily = null
    }

    private fun showMoney(){
        val actualMoney = findViewById<TextView>(R.id.tvDineroDisponible)
        actualMoney.text = "${child.getActualMoney()}€"
    }

    private fun showCashFlow() {
        adapter = ExpenseAdapter(child.getCashFlow())
        val recyclerView = findViewById<RecyclerView>(R.id.rvCashFlow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (child.getCashFlow().isNotEmpty()){
            recyclerView.adapter = adapter
        }else{
            Toast.makeText(this,"No existen movimientos", Toast.LENGTH_SHORT).show()
        }
    }
}