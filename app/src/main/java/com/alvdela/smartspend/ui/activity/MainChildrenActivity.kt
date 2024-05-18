package com.alvdela.smartspend.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.ui.Animations
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.Utility
import com.alvdela.smartspend.ui.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.ui.adapter.ExpenseAdapter
import com.alvdela.smartspend.model.CashFlow
import com.alvdela.smartspend.model.CashFlowType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.filters.DecimalDigitsInputFilter
import com.alvdela.smartspend.firebase.Constants
import com.alvdela.smartspend.firebase.Constants.FAMILY
import com.alvdela.smartspend.firebase.Constants.GOALS
import com.alvdela.smartspend.firebase.Constants.MEMBERS
import com.alvdela.smartspend.firebase.Constants.TASKS
import com.alvdela.smartspend.model.GoalType
import com.alvdela.smartspend.model.SavingGoal
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.ui.adapter.GoalAdapter
import com.alvdela.smartspend.ui.adapter.TaskMandatoryAdapter
import com.alvdela.smartspend.ui.adapter.TaskNoMandatoryAdapter
import com.alvdela.smartspend.ui.fragment.ProfileFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.time.LocalDate

class MainChildrenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val MAX_DECIMALS = 2

    //Menu emergente
    private lateinit var drawer: DrawerLayout

    //Pop up
    private lateinit var dialog: Dialog

    //Adapters de los RecycleView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var mandatoryTaskAdapter: TaskMandatoryAdapter
    private lateinit var noMandatoryTaskAdapter: TaskNoMandatoryAdapter
    private lateinit var goalAdapter: GoalAdapter

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

    //Informacion de la familia y miembro actual
    private var user: String = ""
    private val family = ContextFamily.family!!
    private val isMock = ContextFamily.isMock
    private lateinit var child: Child

    private var uid = "mock"

    //Control de la interfaz
    private var expenses = true
    private var tareas = false
    private var goals = false
    private var games = false

    var record = mutableListOf("Gasto")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_children)
        user = intent.getStringExtra("USER_NAME").toString()
        child = family.getMember(user) as Child
        if (!isMock){
            uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        }
        initObjects()
        showMoney()
        showCashFlow()
        showTask()
        showGoals()
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

        initButtons()
        initToolBar()
        initNavView()
    }

    private fun initButtons() {
        expensesButton.setOnClickListener {
            if (!expenses) {
                restartButtons()
                changeButtonState(expensesButton)
                animateExpenses()
            }
        }
        taskButton.setOnClickListener {
            if (!tareas) {
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }
        }
        goalsButton.setOnClickListener {
            if (!goals) {
                restartButtons()
                changeButtonState(goalsButton)
                animateGoals()
            }
        }
        gameButton.setOnClickListener {
            if (!games) {
                restartButtons()
                changeButtonState(gameButton)
                animateGames()
            }
        }

        addSpentButton.setOnClickListener {
            addSpent()
        }
        addGoalButton.setOnClickListener {
            addSaveGoal()
        }
    }

    /* Metodos para los objetivos de ahorro */

    private fun addSaveGoal() {
        showPopUp(R.layout.pop_up_add_goal)

        val inputDescripcion = dialog.findViewById<EditText>(R.id.inputDescripcionObjetivo)
        inputDescripcion.setText("")

        val inputCantidad = dialog.findViewById<EditText>(R.id.inputCantidadObjetivo)
        inputCantidad.setText("")
        inputCantidad.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group_options)
        var tipo = 0
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = dialog.findViewById<RadioButton>(checkedId)
            tipo = selectedRadioButton.tag.toString().toInt()
        }

        val cancel = dialog.findViewById<Button>(R.id.cancelNewGoal)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val addNewGoal = dialog.findViewById<Button>(R.id.addNewGoal)
        addNewGoal.setOnClickListener {
            var allOk = true
            var descripcion = ""
            var cantidad = BigDecimal(0)
            if (inputDescripcion.text.toString().isBlank()) {
                inputDescripcion.error = "¡Ups! Parece que olvidaste agregar una descripción"
                allOk = false
            } else {
                descripcion = inputDescripcion.text.toString()
            }
            if (inputCantidad.text.toString().isBlank()) {
                inputCantidad.error = "Necesitas saber el precio de lo que deseas conseguir"
                allOk = false
            } else {
                var cantidadString = inputCantidad.text.toString()
                cantidad = cantidadString.toBigDecimal()
            }
            if (allOk) {
                val goal: SavingGoal = when (tipo) {
                    1 -> SavingGoal(descripcion, cantidad, GoalType.TOYS)
                    2 -> SavingGoal(descripcion, cantidad, GoalType.ACTIVITIES)
                    3 -> SavingGoal(descripcion, cantidad, GoalType.GIFT)
                    4 -> SavingGoal(descripcion, cantidad, GoalType.BOOK)
                    else -> SavingGoal(descripcion, cantidad, GoalType.TOYS)

                }
                if (!isMock){
                    addGoalToDatabase(child,goal)
                }else{
                    child.addGoal(goal)
                    goalAdapter.notifyItemInserted(child.getGoals().size)
                }
                dialog.dismiss()
            }
        }
    }

    private fun saveMoney(selectedGoal: Int) {
        val goal = child.getGoals()[selectedGoal]
        showPopUp(R.layout.pop_up_save_money)

        val npNumber = dialog.findViewById<NumberPicker>(R.id.npNumber)
        val editText: EditText = npNumber.getChildAt(0) as EditText
        editText.inputType = InputType.TYPE_NULL
        val npDecimal = dialog.findViewById<NumberPicker>(R.id.npDecimal)
        val editText2: EditText = npDecimal.getChildAt(0) as EditText
        editText2.inputType = InputType.TYPE_NULL

        npNumber.minValue = 0
        npNumber.wrapSelectorWheel = true
        npNumber.setFormatter { i -> String.format("%02d", i) }
        npNumber.value = 0
        npDecimal.minValue = 0
        npDecimal.wrapSelectorWheel = true
        npDecimal.setFormatter { i -> String.format("%2d0", i) }
        npDecimal.value = 0

        var natural = 0
        var decimal = 0

        if (child.getActualMoney() < goal.getMoneyLeft()){
            natural = Utility.getNaturalNumber(child.getActualMoney())
            decimal = Utility.getDecimalNumber(child.getActualMoney())
            npNumber.maxValue = natural
            if (natural <= 0){
                npDecimal.maxValue = Utility.getDecimalNumber(child.getActualMoney())
                npDecimal.setFormatter { i -> String.format("%02d", i) }
            }else{
                npDecimal.maxValue = 99
                npDecimal.setFormatter { i -> String.format("%02d", i) }
            }
        }else{
            natural = Utility.getNaturalNumber(goal.getMoneyLeft())
            decimal = Utility.getDecimalNumber(goal.getMoneyLeft())
            npNumber.maxValue = natural
            if (npNumber.maxValue <= 0){
                npDecimal.maxValue = decimal
                npDecimal.setFormatter { i -> String.format("%02d", i) }
            }else{
                npDecimal.maxValue = 99
                npDecimal.setFormatter { i -> String.format("%02d", i) }
            }
        }

        npNumber.setOnValueChangedListener { _, _, newVal ->
            if (newVal == npNumber.maxValue && decimal == 0){
                npDecimal.value = 0
                npDecimal.isEnabled = false
            }else if(newVal == npNumber.maxValue && decimal != 0){
                npDecimal.maxValue = decimal
                npDecimal.setFormatter { i -> String.format("%02d", i) }
                npDecimal.isEnabled = true
            }else{
                npDecimal.isEnabled = true
                npDecimal.maxValue = 99
                npDecimal.setFormatter { i -> String.format("%02d", i) }
            }
        }

        val cancelButtonSave = dialog.findViewById<Button>(R.id.cancelButtonSave)
        cancelButtonSave.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButtonSave = dialog.findViewById<Button>(R.id.confirmButtonSave)
        confirmButtonSave.setOnClickListener {
            val value = Utility.formBigDecimalNumber(npNumber.value,npDecimal.value)

            child.setActualMoney(child.getActualMoney() - value + goal.saveMoney(value))
            if (!isMock){
                updateGoalToDatabase(child.getId(),goal)
                setMoneyInDatabase(child.getActualMoney())
            }else{
                showMoney()
            }
            dialog.dismiss()
            goalAdapter.notifyItemChanged(selectedGoal)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun extractMoney(selectedGoal: Int) {
        val goal = child.getGoals()[selectedGoal]
        if (goal.isArchived()){
            showPopUp(R.layout.pop_up_goal_archieved)
            val confirmGoal = dialog.findViewById<Button>(R.id.confirmGoalAchieved)
            confirmGoal.setOnClickListener {
                if (!isMock){
                    deleteGoalFromDatabase(child.getId(),child.getGoals()[selectedGoal].getId())
                }
                child.claimGoal(selectedGoal)
                goalAdapter.notifyItemRemoved(selectedGoal)
                showGoals()
                expenseAdapter.notifyDataSetChanged()

                dialog.dismiss()
            }
        }else{
            showPopUp(R.layout.pop_up_delete)
            val tvDelete = dialog.findViewById<TextView>(R.id.tvDelete)
            tvDelete.text = resources.getString(R.string.extract_money_of_goal)
            tvDelete.textSize = 18f
            val cancelDelete = dialog.findViewById<Button>(R.id.cancelDelete)
            cancelDelete.setOnClickListener {
                dialog.dismiss()
            }
            val confirmDelete = dialog.findViewById<Button>(R.id.confirmDelete)
            confirmDelete.text = resources.getString(R.string.aceptar)
            confirmDelete.setOnClickListener {
                if (!isMock){
                    deleteGoalFromDatabase(child.getId(),child.getGoals()[selectedGoal].getId())
                }
                child.claimGoal(selectedGoal)
                goalAdapter.notifyItemRemoved(selectedGoal)
                expenseAdapter.notifyDataSetChanged()
                showGoals()
                dialog.dismiss()
            }
        }
        Handler().postDelayed({
            showMoney()
        }, 2000)
    }

    /* Metodos para las tareas */
    private fun completeTask(selectedTask: Int) {
        val task = family.getTask(selectedTask)
        task.setState(TaskState.COMPLETE)
        task.setChild(child)
        if (!isMock){
            updateTaskInDatabase(task, TASKS)
        }
        if (task.isMandatory()) {
            mandatoryTaskAdapter.removeItem()
        } else {
            noMandatoryTaskAdapter.removeItem()
        }
    }

    /* Metodos para los gastos*/
    @SuppressLint("NotifyDataSetChanged")
    private fun addSpent() {
        showPopUp(R.layout.pop_up_add_spent)

        val descripcion = dialog.findViewById<AutoCompleteTextView>(R.id.inputDescripcionGasto)
        val amount = dialog.findViewById<EditText>(R.id.inputCantidadGasto)
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val addButton = dialog.findViewById<Button>(R.id.addNewSpent)

        descripcion.setText("")
        val autoComplete = CustomSpinnerAdapter(this, record.toList())
        descripcion.setAdapter(autoComplete)
        descripcion.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descripcion.showDropDown()
        }

        amount.setText("")
        amount.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        var tipo = 0
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            tipo = selectedRadioButton.tag.toString().toInt()
        }

        val cancelNewSpent = dialog.findViewById<Button>(R.id.cancelNewSpent)
        cancelNewSpent.setOnClickListener {
            dialog.dismiss()
        }

        addButton.setOnClickListener {
            val descripcionText = descripcion.text.toString()
            if (descripcionText.isEmpty()) {
                descripcion.error = "Se necesita una descripción"
                Toast.makeText(this, "Se necesita una descripción", Toast.LENGTH_SHORT).show()
            } else if (amount.text.toString().isEmpty()) {
                amount.error = "La cantidad no puede quedar vacia"
                Toast.makeText(this, "La cantidad no puede quedar vacia", Toast.LENGTH_SHORT).show()
            } else if (amount.text.toString().toBigDecimal() > child.getActualMoney()) {
                amount.error = "La cantidad no puede ser mayor que la cantidad disponible"
                Toast.makeText(
                    this,
                    "La cantidad no puede ser mayor que la cantidad disponible",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val amountNumber = amount.text.toString().toBigDecimal()
                addRecord(descripcionText)
                when (tipo) {
                    1 -> {
                        val newExpense = CashFlow(
                            descripcionText, amountNumber, CashFlowType.COMIDA,
                            LocalDate.now()
                        )
                        child.addExpense(newExpense)
                    }

                    2 -> {
                        val newExpense = CashFlow(
                            descripcionText, amountNumber, CashFlowType.COMPRAS,
                            LocalDate.now()
                        )
                        child.addExpense(newExpense)
                    }

                    3 -> {
                        val newExpense = CashFlow(
                            descripcionText, amountNumber, CashFlowType.OCIO,
                            LocalDate.now()
                        )
                        child.addExpense(newExpense)
                    }

                    else -> {
                        val newExpense = CashFlow(
                            descripcionText, amountNumber, CashFlowType.OTROS,
                            LocalDate.now()
                        )
                        child.addExpense(newExpense)
                    }
                }
                Handler().postDelayed({
                    expenseAdapter.notifyDataSetChanged()
                    showMoney()
                }, 1000)
                dialog.dismiss()
            }
        }
    }

    /* Metodo para actualizar el dinero disponible */
    private fun showMoney() {
        val actualMoney = findViewById<TextView>(R.id.tvDineroDisponible)
        actualMoney.text = "${child.getActualMoney()}€"
    }

    /* Metodos para inicializar los RecycleView*/

    private fun showTask() {
        mandatoryTaskAdapter = TaskMandatoryAdapter(
            tasks = family.getTaskList()
        ) { selectedTask ->
            completeTask(
                selectedTask
            )
        }
        val rvTaskObligatorias = findViewById<RecyclerView>(R.id.rvTaskObligatorias)
        rvTaskObligatorias.layoutManager = LinearLayoutManager(this)
        rvTaskObligatorias.adapter = mandatoryTaskAdapter
        rvTaskObligatorias.itemAnimator = DefaultItemAnimator()

        noMandatoryTaskAdapter = TaskNoMandatoryAdapter(
            tasks = family.getTaskList()
        ) { selectedTask ->
            completeTask(
                selectedTask
            )
        }
        val rvTaskExtra = findViewById<RecyclerView>(R.id.rvTaskExtra)
        rvTaskExtra.layoutManager = LinearLayoutManager(this)
        rvTaskExtra.adapter = noMandatoryTaskAdapter
        rvTaskExtra.itemAnimator = DefaultItemAnimator()
    }

    private fun showGoals() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvGoals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        goalAdapter = GoalAdapter(
            goals = child.getGoals(),
            saveMoney = { selectedGoal -> saveMoney(selectedGoal) },
            extractTask = { selectedGoal -> extractMoney(selectedGoal) }
        )
        recyclerView.adapter = goalAdapter
    }

    private fun showCashFlow() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvCashFlow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(child.getCashFlow())
        recyclerView.adapter = expenseAdapter
    }

    /* Metodo encargado de los pop ups*/

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    /* Metodos para las animaciones*/
    private fun changeButtonState(button: ImageView) {
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
        button.setColorFilter(ContextCompat.getColor(this, R.color.mid_gray))
    }

    private fun restartButtons() {
        expensesButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        taskButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        goalsButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        gameButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))

        expensesButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
        taskButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
        goalsButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
        gameButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
    }

    private fun animateExpenses() {
        if (tareas) {
            Animations.animateViewOfFloat(taskLayout, "translationX", 2000f, 300)
        }
        if (goals) {
            Animations.animateViewOfFloat(goalsLayout, "translationX", 2000f, 300)
        }
        if (games) {
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
        if (expenses) {
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
            taskLayout.translationX = 2000f
        }
        if (goals) {
            Animations.animateViewOfFloat(goalsLayout, "translationX", 2000f, 300)
            taskLayout.translationX = -2000f
        }
        if (games) {
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
        if (expenses) {
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
            goalsLayout.translationX = 2000f
        }
        if (tareas) {
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
            goalsLayout.translationX = 2000f
        }
        if (games) {
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
        if (expenses) {
            Animations.animateViewOfFloat(expensesLayout, "translationX", -2000f, 300)
        }
        if (goals) {
            Animations.animateViewOfFloat(goalsLayout, "translationX", -2000f, 300)
        }
        if (tareas) {
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
        }
        gameLayout.translationX = 2000f
        Animations.animateViewOfFloat(gameLayout, "translationX", 0f, 300)
        expenses = false
        tareas = false
        goals = false
        games = true
    }

    /* Metodos de control del activity */
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

    }

    @Deprecated("Deprecated")
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }else if (ProfileFragment.configProfileOpen) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentProfile)
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            ProfileFragment.configProfileOpen = false
        } else {
            showPopUp(R.layout.pop_up_back_profiles)

            val cancelButton = dialog.findViewById<Button>(R.id.cancelButtonBackProfiles)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            val confirmButton = dialog.findViewById<Button>(R.id.confirmButtonBackProfiles)
            confirmButton.setOnClickListener {
                dialog.dismiss()
                super.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_item_settings -> showEditProfile()
            R.id.nav_item_signout -> signOut()
            R.id.nav_item_backprofiles -> backProfiles()
            R.id.nav_item_share -> share()
        }

        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun share() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "¡Descubre la nueva forma de enseñar finanzas a tus hijos!")
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "SmartSpend")
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir mediante"))
    }

    private fun backProfiles() {
        startActivity(Intent(this, ProfilesActivity::class.java))
    }

    private fun signOut() {
        startActivity(Intent(this, LoginActivity::class.java))
        ContextFamily.reset()
    }

    private fun addRecord(newString: String){
        var existe = false
        for (i in record){
            if (newString == i) existe = true
        }
        if (!existe){
            record.add(0,newString)
        }
        if (record.size > 3){
            record.removeAt(record.size - 1)
        }
    }

    private fun showEditProfile() {
        val fragmentView = findViewById<FragmentContainerView>(R.id.fragmentProfile)
        val bundle = bundleOf(ProfileFragment.USER_BUNDLE to user)
        ProfileFragment.configProfileOpen = true
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<ProfileFragment>(R.id.fragmentProfile, args = bundle)
        }
        fragmentView.translationX = 500f
        Animations.animateViewOfFloat(fragmentView, "translationX", 0f, 300)
    }

    /* Operaciones de Firebase */

    private fun updateTaskInDatabase(task: Task, typeOfTask: String) {
        var completedDate = ""
        if (typeOfTask == Constants.HISTORIC && task.getCompletedDate() != null) {
            completedDate = task.getCompletedDate()!!.format(Constants.dateFormat)
        }
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(typeOfTask)
            .document(task.getId())
            .update(
                mapOf(
                    "state" to TaskState.toString(task.getState()),
                    "completedDate" to completedDate,
                    "child" to task.getChildName()
                )
            )
            .addOnSuccessListener {
                println("Documento actualizado exitosamente.")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar documento: $e")
            }
    }

    private fun addGoalToDatabase(child: Child, goal: SavingGoal) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getId())
            .collection(GOALS)
            .add(
                hashMapOf(
                    "description" to goal.getDescription(),
                    "goal" to goal.getGoal().toString(),
                    "type" to GoalType.toString(goal.getType()),
                    "saving" to goal.getSaving().toString(),
                    "archived" to goal.isArchived()
                )
            )
            .addOnSuccessListener { document ->
                goal.setId(document.id)
                child.addGoal(goal)
                goalAdapter.notifyItemInserted(child.getGoals().size)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar el objetivo", Toast.LENGTH_SHORT).show()
                println("Error al agregar el objetivo: $e")
            }
    }

    private fun updateGoalToDatabase(childId: String, goal: SavingGoal) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(childId)
            .collection(GOALS)
            .document(goal.getId()) // Suponiendo que tienes un ID para la meta
            .update(
                mapOf(
                    "saving" to goal.getSaving().toString(),
                    "archived" to goal.isArchived()
                )
            )
            .addOnSuccessListener {
                println("Meta actualizada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar meta: $e")
            }
    }

    private fun deleteGoalFromDatabase(childId: String, goalId: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(childId)
            .collection(GOALS)
            .document(goalId)
            .delete()
            .addOnSuccessListener {
                println("Meta eliminada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar meta: $e")
            }
    }

    private fun setMoneyInDatabase(money: BigDecimal){
        FirebaseFirestore.getInstance()
            .collection(FirebaseAuth.getInstance().currentUser!!.uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(child.getId())
            .update(
                mapOf(
                    "money" to money.toString()
                )
            )
            .addOnSuccessListener {
                showMoney()
                println("Dinero actualizado correctamente")
            }
            .addOnFailureListener {
                println("Error al actualizar el dinero disponible")
            }
    }
}