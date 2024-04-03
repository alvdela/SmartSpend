package com.alvdela.smartspend

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.adapter.ExpenseAdapter
import com.alvdela.smartspend.domain.Child
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.domain.Parent
import com.google.android.material.navigation.NavigationView

class MainParentsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var family: Family
    private var user: String? = null
    private var seguimiento = true
    private var tareas = false
    private var administracion = false

    private lateinit var drawer: DrawerLayout
    private lateinit var seleccionarMiembro: Spinner
    private lateinit var seguimientoButton: ImageView
    private lateinit var taskButton: ImageView
    private lateinit var adminButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_parents)
        user = intent.getStringExtra("USER_NAME")
        getFamily()
        initObjects()
        initToolBar()
        initNavView()
        initSpinner()
    }

    private fun initObjects() {
        seleccionarMiembro = findViewById(R.id.botonSeleccionarMiembro)
        seguimientoButton = findViewById(R.id.seguimiento_button)
        taskButton = findViewById(R.id.task_button)
        adminButton = findViewById(R.id.admin_button)

        val currentUserImage = findViewById<ImageView>(R.id.ivCurrentUserImage)
        val currentUserName = findViewById<TextView>(R.id.tvCurrentUserName)
        currentUserName.text = user
    }

    private fun initSpinner(){
        val options = family.getChildrenNames()
        println(options)
        val adapter = CustomSpinnerAdapter(this, options)
        seleccionarMiembro.adapter = adapter
        seleccionarMiembro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedOption = options[position]
                if (selectedOption != "") showCashFlow(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //Do nothing
            }
        }
    }

    private fun showCashFlow(child: String) {

        val childSelected = family.getMember(child) as Child
        val cashFlowList = childSelected.getCashFlow().toList()
        println(cashFlowList)
        val recyclerView = findViewById<RecyclerView>(R.id.rvCashFlow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (cashFlowList.isNotEmpty()){
            recyclerView.adapter = ExpenseAdapter(cashFlowList)
        }else{
            Toast.makeText(this,"No existen movimientos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFamily() {
        if (ContextFamily.mockFamily != null) {
            family = ContextFamily.mockFamily!!
        } else {
            //TODO consulta a firebase
        }
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