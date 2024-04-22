package com.alvdela.smartspend.ui.activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
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
import com.alvdela.smartspend.ui.Animations
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.ui.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.ui.adapter.ExpenseAdapter
import com.alvdela.smartspend.ui.adapter.MemberAdapter
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.model.Parent
import com.google.android.material.navigation.NavigationView

class MainParentsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var family: Family
    private var user: String = ""
    private var seguimiento = true
    private var tareas = false
    private var administracion = false

    private lateinit var drawer: DrawerLayout
    private lateinit var seleccionarMiembro: Spinner
    private lateinit var seguimientoButton: ImageView
    private lateinit var taskButton: ImageView
    private lateinit var adminButton: ImageView
    private lateinit var seguimientoLayout: ConstraintLayout
    private lateinit var taskLayout: ConstraintLayout
    private lateinit var adminLayout: ConstraintLayout

    private lateinit var dialog: Dialog
    private lateinit var memberAdapter: MemberAdapter

    private val MAX_USER_LENGHT = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_parents)
        user = intent.getStringExtra("USER_NAME").toString()
        getFamily()
        initObjects()
        initToolBar()
        initNavView()
        initSpinner()
        showMembers()
    }

    private fun initObjects() {
        seleccionarMiembro = findViewById(R.id.botonSeleccionarMiembro)
        seguimientoButton = findViewById(R.id.seguimiento_button)
        taskButton = findViewById(R.id.task_button)
        adminButton = findViewById(R.id.admin_button)
        seguimientoLayout = findViewById(R.id.consultarLayout)
        taskLayout = findViewById(R.id.tareasLayout)
        adminLayout = findViewById(R.id.adminLayout)

        //val currentUserImage = findViewById<ImageView>(R.id.ivCurrentUserImage)
        val currentUserName = findViewById<TextView>(R.id.tvCurrentUserName)
        currentUserName.text = user
        changeButtonState(seguimientoButton)

        seguimientoButton.setOnClickListener {
            if (!seguimiento){
                restartButtons()
                changeButtonState(seguimientoButton)
                animateSegimiento()
            }
        }
        taskButton.setOnClickListener {
            if (!tareas){
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }
        }
        adminButton.setOnClickListener {
            if (!administracion){
                restartButtons()
                changeButtonState(adminButton)
                animateAdministracion()
            }
        }

        val addMemberButton = findViewById<Button>(R.id.buttonAddMember)
        addMemberButton.setOnClickListener {
            addMember()
        }
    }

    private fun addMember() {
        showPopUp(R.layout.pop_up_add_member)

        val memberNameWarning = dialog.findViewById<TextView>(R.id.tvMiembroExistente)
        memberNameWarning.visibility = View.INVISIBLE
        val passwordWarning = dialog.findViewById<TextView>(R.id.tv_advise_password2)
        passwordWarning.visibility = View.INVISIBLE

        var tipo = 2
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.rgMemberButtons)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = dialog.findViewById<RadioButton>(checkedId)
            tipo = selectedRadioButton.tag.toString().toInt()
        }
        val userName = dialog.findViewById<EditText>(R.id.inputNombreUsuario)
        userName.setText("")
        val passwordInput = dialog.findViewById<EditText>(R.id.passwordAddMember)
        passwordInput.setText("")
        val passwordInputRepeat = dialog.findViewById<EditText>(R.id.passwordAddMemberAgain)
        passwordInputRepeat.setText("")

        val cancel = dialog.findViewById<Button>(R.id.cancelNewMember)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val addNewMember = dialog.findViewById<Button>(R.id.addNewMember)
        addNewMember.setOnClickListener {
            memberNameWarning.visibility = View.INVISIBLE
            passwordWarning.visibility = View.INVISIBLE
            var memberName = ""
            if (userName.text.isEmpty()){
                userName.error = "Se necesita un nombre de usuario"
            }else{
                memberName = userName.text.toString()
            }
            if (memberName != ""){
                if (family.checkName(userName.text.toString())){
                    memberNameWarning.visibility = View.VISIBLE
                }else if (userName.text.toString().length > MAX_USER_LENGHT){
                    userName.error = "Nombre demasiado largo. Máximo 10 caracteres."
                }else if(passwordInput.text.toString() != passwordInputRepeat.text.toString()){
                    passwordWarning.visibility = View.VISIBLE
                }else{
                    when(tipo){
                        1 -> {
                            val parent = Parent(memberName,passwordInput.text.toString())
                            val result = family.addMember(parent)
                            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                        }
                        2 -> {
                            val child = Child(memberName, passwordInput.text.toString())
                            val result = family.addMember(child)
                            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                        }
                        else ->{
                            Toast.makeText(this,"Ha ocurrido un error inesperado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                    memberAdapter.notifyItemInserted(family.getMembers().size)
                }
            }
        }
    }

    private fun animateAdministracion() {
        if (tareas){
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
            //taskLayout.visibility = View.GONE
        }
        if (seguimiento){
            Animations.animateViewOfFloat(seguimientoLayout, "translationX", -2000f, 300)
            //seguimientoLayout.visibility = View.GONE
        }
        //adminLayout.visibility = View.VISIBLE
        adminLayout.translationX = 2000f
        Animations.animateViewOfFloat(adminLayout, "translationX", 0f, 300)
        seguimiento = false
        tareas = false
        administracion = true
    }

    private fun animateTareas() {
        if (seguimiento){
            Animations.animateViewOfFloat(seguimientoLayout, "translationX", -2000f, 300)
            //seguimientoLayout.visibility = View.GONE
            taskLayout.translationX = 2000f
        }
        if (administracion){
            Animations.animateViewOfFloat(adminLayout, "translationX", 2000f, 300)
            //adminLayout.visibility = View.GONE
            taskLayout.translationX = -2000f
        }
        //taskLayout.visibility = View.VISIBLE
        Animations.animateViewOfFloat(taskLayout, "translationX", 0f, 300)
        seguimiento = false
        tareas = true
        administracion = false
    }

    private fun animateSegimiento() {
        if (tareas){
            Animations.animateViewOfFloat(taskLayout, "translationX", 2000f, 300)
            //taskLayout.visibility = View.GONE
        }
        if (administracion){
            Animations.animateViewOfFloat(adminLayout, "translationX", 2000f, 300)
            //adminLayout.visibility = View.GONE
        }
        seguimientoLayout.translationX = -2000f
        //seguimientoLayout.visibility = View.VISIBLE
        Animations.animateViewOfFloat(seguimientoLayout, "translationX", 0f, 300)
        seguimiento = true
        tareas = false
        administracion = false
    }

    private fun changeButtonState(button: ImageView) {
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
        button.setColorFilter(ContextCompat.getColor(this, R.color.mid_gray))
    }

    private fun restartButtons() {
        seguimientoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        taskButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))
        adminButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue))

        seguimientoButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
        taskButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
        adminButton.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
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
        val recyclerView = findViewById<RecyclerView>(R.id.rvCashFlow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (childSelected.getCashFlow().isNotEmpty()){
            recyclerView.adapter = ExpenseAdapter(childSelected.getCashFlow())
        }else{
            Toast.makeText(this,"No existen movimientos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMembers(){
        memberAdapter = MemberAdapter(
            memberMap = family.getMembers(),
            editMember = {selectedMember -> editMember(selectedMember)},
            deleteMember = {selectedMember -> deleteMember(selectedMember)},
            addAllowance = { selectedMember -> addAllowance(selectedMember)},
            editAllowance = {allowanceId,selectedChild -> editAllowance(allowanceId,selectedChild)},
            deleteAllowance = {allowanceId,selectedChild -> deleteAllowance(allowanceId,selectedChild)},
            this
        )
        val recyclerView = findViewById<RecyclerView>(R.id.rvAdminFamilia)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (family.getMembers().isNotEmpty()){
            recyclerView.adapter = memberAdapter
        }else{
            //TODO cerrar aplicacion y borrar familia
        }
    }

    private fun deleteAllowance(allowanceId: Int, selectedChild: String) {
        Toast.makeText(this,"Eliminar $allowanceId, $selectedChild", Toast.LENGTH_SHORT).show()
    }

    private fun editAllowance(allowanceId: Int, selectedChild: String) {
        Toast.makeText(this,"Editar $allowanceId, $selectedChild", Toast.LENGTH_SHORT).show()
    }

    private fun addAllowance(selectedMember: String) {
        Toast.makeText(this,"Añadir asignación $selectedMember", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMember(selectedMember: String) {
        showPopUp(R.layout.pop_up_delete_member)

        val cancel = dialog.findViewById<Button>(R.id.cancelDeleteMember)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmDeleteMember)
        confirmButton.setOnClickListener {
            family.deleteMember(selectedMember)
            dialog.dismiss()
            memberAdapter.notifyItemRemoved(family.getMembers().size)
        }
    }


    private fun editMember(selectedMember: String) {
        showPopUp(R.layout.pop_up_add_member)
        val member = family.getMember(selectedMember)!!

        val memberNameWarning = dialog.findViewById<TextView>(R.id.tvMiembroExistente)
        memberNameWarning.visibility = View.INVISIBLE
        val passwordWarning = dialog.findViewById<TextView>(R.id.tv_advise_password2)
        passwordWarning.visibility = View.INVISIBLE

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.rgMemberButtons)
        radioGroup.visibility = View.GONE

        val userName = dialog.findViewById<EditText>(R.id.inputNombreUsuario)
        userName.setText(member.getUser())
        val passwordInput = dialog.findViewById<EditText>(R.id.passwordAddMember)
        passwordInput.setText("")
        val passwordInputRepeat = dialog.findViewById<EditText>(R.id.passwordAddMemberAgain)
        passwordInputRepeat.setText("")

        val cancel = dialog.findViewById<Button>(R.id.cancelNewMember)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val header = dialog.findViewById<TextView>(R.id.tvPopUpAddMember)
        header.text = resources.getString(R.string.pop_up_edit_member)

        val editMember = dialog.findViewById<Button>(R.id.addNewMember)
        editMember.text = resources.getString(R.string.confirmar)
        editMember.setOnClickListener {
            memberNameWarning.visibility = View.INVISIBLE
            passwordWarning.visibility = View.INVISIBLE
            var memberName = ""
            if (userName.text.isEmpty()){
                userName.error = "Se necesita un nombre de usuario"
            }else{
                memberName = userName.text.toString()
            }
            if (memberName != ""){
                if (family.checkName(userName.text.toString()) && userName.text.toString() != selectedMember){
                    memberNameWarning.visibility = View.VISIBLE
                }else if (userName.text.toString().length > MAX_USER_LENGHT){
                    userName.error = "Nombre demasiado largo. Máximo 10 caracteres."
                }else if(passwordInput.text.toString() != passwordInputRepeat.text.toString()){
                    passwordWarning.visibility = View.VISIBLE
                }else{
                    family.deleteMember(selectedMember)
                    member.setUser(memberName)
                    member.setPassword(passwordInput.text.toString())
                    family.addMember(member)
                    dialog.dismiss()
                    memberAdapter.notifyDataSetChanged()
                }
            }

        }
    }

    private fun getFamily() {
        if (ContextFamily.isMock) {
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

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showDatePickerDialog(text: EditText) {
        val newFragment = DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = day.toString() + " / " + (month + 1) + " / " + year
            text.setText(selectedDate)
        })

        newFragment.show(supportFragmentManager, "datePicker")
    }
}