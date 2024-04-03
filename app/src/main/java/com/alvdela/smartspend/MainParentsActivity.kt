package com.alvdela.smartspend

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.adapter.ExpenseAdapter
import com.alvdela.smartspend.domain.Child
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.domain.Parent

class MainParentsActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout

    private lateinit var family: Family
    private lateinit var user: Parent

    private lateinit var seleccionarMiembro: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_parents)
        getFamily()
        initObjects()
        initSpinner()
    }

    private fun initObjects() {
        seleccionarMiembro = findViewById(R.id.botonSeleccionarMiembro)
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
        val userName = intent.getStringExtra("USER_NAME")
        if (userName != null) {
            user = family.getMember(userName) as Parent
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
}