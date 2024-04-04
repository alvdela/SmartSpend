package com.alvdela.smartspend

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainChildrenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    //TextView para mostrar el dinero disponible
    private lateinit var tvDineroDisponible: TextView
    
    //Botones para añadir elementos
    private lateinit var addSpentButton: Button
    private lateinit var addTaskButton: Button
    private lateinit var addGoalButton: Button
    
    private var user : String? = null

    private var expenses = true
    private var tareas = false
    private var goals = false
    private var games = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_children)
        user = intent.getStringExtra("USER_NAME")
        initObjects()
        initToolBar()
        initNavView()
    }

    private fun initObjects() {
        expensesLayout = findViewById(R.id.expensesLayout)
        tvDineroDisponible = findViewById(R.id.tvDineroDisponible)
        addSpentButton = findViewById(R.id.buttonAddSpent)
        taskLayout = findViewById(R.id.tareasLayout)
        addTaskButton = findViewById(R.id.buttonAddTask)
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
    }

    private fun animateExpenses() {
        TODO("Not yet implemented")
    }
    private fun animateTareas() {
        TODO("Not yet implemented")
    }
    private fun animateGoals() {
        TODO("Not yet implemented")
    }
    private fun animateGames() {
        TODO("Not yet implemented")
    }

    private fun changeButtonState(button: ImageView) {
        button.setBackgroundColor(ContextCompat.getColor(this,R.color.light_blue))
        button.setColorFilter(ContextCompat.getColor(this,R.color.mid_grey))
    }

    private fun restartButtons() {
        expensesButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        taskButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        goalsButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))
        gameButton.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_blue))

        expensesButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_grey))
        taskButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_grey))
        goalsButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_grey))
        gameButton.setColorFilter(ContextCompat.getColor(this,R.color.dark_grey))
    }

    private fun initToolBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.main_parents)
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
            //TODO mostrar mensaje si quiere salir
            super.onBackPressed()
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
}