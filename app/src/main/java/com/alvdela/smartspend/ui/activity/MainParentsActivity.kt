package com.alvdela.smartspend.ui.activity

import DatePickerFragment
import TaskCompleteAdapter
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.adapter.CustomSpinnerAdapter
import com.alvdela.smartspend.adapter.ExpenseAdapter
import com.alvdela.smartspend.adapter.MemberAdapter
import com.alvdela.smartspend.adapter.TaskOpenAdapter
import com.alvdela.smartspend.filters.DecimalDigitsInputFilter
import com.alvdela.smartspend.filters.Validator
import com.alvdela.smartspend.model.Allowance
import com.alvdela.smartspend.model.AllowanceType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Member
import com.alvdela.smartspend.model.MemberType
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.ui.Animations
import com.alvdela.smartspend.ui.fragment.GraphFragment
import com.alvdela.smartspend.ui.fragment.ProfileFragment
import com.alvdela.smartspend.ui.fragment.ProfileFragment.Companion.USER_BUNDLE
import com.alvdela.smartspend.ui.fragment.TaskHistoryFragment
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.util.Constants.ALLOWANCES
import com.alvdela.smartspend.util.Constants.CASHFLOW
import com.alvdela.smartspend.util.Constants.FAMILY
import com.alvdela.smartspend.util.Constants.GOALS
import com.alvdela.smartspend.util.Constants.HISTORIC
import com.alvdela.smartspend.util.Constants.MEMBERS
import com.alvdela.smartspend.util.Constants.TASKS
import com.alvdela.smartspend.util.Constants.dateFormat
import com.alvdela.smartspend.util.CropImage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs


class MainParentsActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, GestureDetector.OnGestureListener{

    //Informacion de la familia y miembro actual
    private val family = ContextFamily.family!!
    private val isMock = ContextFamily.isMock
    private var user = ""

    private var uid = "mock"

    private lateinit var membersCopy: MutableMap<String, Member>

    // Variables para el control de la interfaz
    private var seguimiento = true
    private var tareas = false
    private var administracion = false

    private var isPendientesShow = true
    private var isCompletadasShow = true

    //Menu emergente y toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    // Difrentes botones
    private lateinit var seleccionarMiembro: Spinner
    private lateinit var seguimientoButton: ImageView
    private lateinit var taskButton: ImageView
    private lateinit var adminButton: ImageView

    // Layouts
    private lateinit var seguimientoLayout: ConstraintLayout
    private lateinit var taskLayout: ConstraintLayout
    private lateinit var adminLayout: ConstraintLayout

    // Variables para menus desplegables
    private lateinit var extendPendientes: ImageView
    private lateinit var containerPendientes: RelativeLayout
    private lateinit var rvTaskPendientes: RecyclerView

    private lateinit var extendCompletadas: ImageView
    private lateinit var containerCompletadas: RelativeLayout
    private lateinit var rvTaskCompletadas: RecyclerView

    // Pop up
    private lateinit var dialog: Dialog

    // Adapters para los RecycleView
    private lateinit var memberAdapter: MemberAdapter
    private lateinit var completeTaskAdapter: TaskCompleteAdapter
    private lateinit var openTaskAdapter: TaskOpenAdapter

    //Constantes
    private val MAX_USER_LENGHT = 15
    private val MAX_DECIMALS = 2

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_parents)
        user = intent.getStringExtra("USER_NAME").toString()
        if (!isMock){
            uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        }
        initObjects()
        initToolBar()
        initNavView()
        initSpinner()
        showTasks()
        showMembers()
        initGestures()
        if (!ContextFamily.isMock) showProfilePicture()
    }

    override fun onStart() {
        super.onStart()
        if (!ContextFamily.isMock) showProfilePicture()
    }

    private fun initObjects() {
        seleccionarMiembro = findViewById(R.id.botonSeleccionarMiembro)
        seguimientoButton = findViewById(R.id.seguimiento_button)
        taskButton = findViewById(R.id.task_button)
        adminButton = findViewById(R.id.admin_button)
        seguimientoLayout = findViewById(R.id.consultarLayout)
        taskLayout = findViewById(R.id.tareasLayout)
        adminLayout = findViewById(R.id.adminLayout)
        extendPendientes = findViewById(R.id.ivExtendPendientes)
        containerPendientes = findViewById(R.id.containerPendientes)
        rvTaskPendientes = findViewById(R.id.rvTaskPendientes)
        extendCompletadas = findViewById(R.id.ivExtendCompletadas)
        containerCompletadas = findViewById(R.id.containerCompletadas)
        rvTaskCompletadas = findViewById(R.id.rvTaskCompletadas)

        val currentUserName = findViewById<TextView>(R.id.tvCurrentUserName)
        currentUserName.text = user
        currentUserName.setOnClickListener {
            showEditProfile()
        }
        val ivCurrentUserImage = findViewById<ImageView>(R.id.ivCurrentUserImage)
        ivCurrentUserImage.setOnClickListener {
            showEditProfile()
        }

        changeButtonState(seguimientoButton)

        seguimientoButton.setOnClickListener {
            if (!seguimiento) {
                restartButtons()
                changeButtonState(seguimientoButton)
                animateSegimiento()
            }
        }
        taskButton.setOnClickListener {
            if (!tareas) {
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }
        }
        adminButton.setOnClickListener {
            if (!administracion) {
                restartButtons()
                changeButtonState(adminButton)
                animateAdministracion()
            }
        }

        extendPendientes.setOnClickListener {
            isPendientesShow = if (isPendientesShow) {
                Animations.compactView(extendPendientes, containerPendientes, rvTaskPendientes)
                false
            } else {
                Animations.extendView(extendPendientes, containerPendientes, rvTaskPendientes)
                true
            }
        }

        extendCompletadas.setOnClickListener {
            isCompletadasShow = if (isCompletadasShow) {
                Animations.compactView(extendCompletadas, containerCompletadas, rvTaskCompletadas)
                false
            } else {
                Animations.extendView(extendCompletadas, containerCompletadas, rvTaskCompletadas)
                true
            }
        }

        val addMemberButton = findViewById<Button>(R.id.buttonAddMember)
        addMemberButton.setOnClickListener {
            addMember()
        }

        val addTaskButton = findViewById<FloatingActionButton>(R.id.buttonAddTask)
        addTaskButton.setOnClickListener {
            createNewTask()
        }
    }

    private fun showProfilePicture() {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid
        val fileName = family.getMember(user)!!.getId()

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$fileName")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                if (localFile.exists() && localFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)

                    if (bitmap != null) {
                        val ivCurrentUserImage = findViewById<ImageView>(R.id.ivCurrentUserImage)
                        ivCurrentUserImage.scaleType = ImageView.ScaleType.FIT_CENTER
                        ivCurrentUserImage.setImageBitmap(CropImage.getCroppedBitmap(bitmap))
                    }
                }
            }
            .addOnFailureListener{
                //Toast.makeText(this, "Fallo al obtener imagen de perfil", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestures(){
        gestureDetector = GestureDetector(this, this)
        val mainView = findViewById<View>(R.id.main_parents)
        mainView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    /* Metodo para la gestion de las tareas */

    private fun closeTask(selectedTask: Int) {
        showPopUp(R.layout.pop_up_delete)
        val tvDelete = dialog.findViewById<TextView>(R.id.tvDelete)
        tvDelete.text = resources.getString(R.string.delete_task)
        val cancel = dialog.findViewById<Button>(R.id.cancelDelete)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmDelete)
        confirmButton.setOnClickListener {
            family.getTask(selectedTask).setCompletedDate(LocalDate.now())
            if (!isMock){
                deleteTaskFromDatabase(family.getTask(selectedTask).getId(), TASKS)
                addTaskToDatabase(family.getTask(selectedTask), HISTORIC)
            }
            family.removeTask(selectedTask)
            dialog.dismiss()
            openTaskAdapter.removeItem()
            ProfilesActivity.updateWidgets(this)
        }

    }

    private fun completeTask(selectedTask: Int) {
        showPopUp(R.layout.pop_up_complete_task)
        val task = family.getTask(selectedTask)
        val tvNota = dialog.findViewById<TextView>(R.id.tvNota)
        if (task.getPrice().compareTo(BigDecimal(0)) == -1 || task.getPrice().compareTo(BigDecimal(0)) == 0) {
            tvNota.visibility = View.GONE
        }
        val reOpenTask = dialog.findViewById<Button>(R.id.reOpenTask)
        reOpenTask.setOnClickListener {
            task.reOpenTask()
            if (!isMock){
                updateTaskInDatabase(task, TASKS)
            }
            completeTaskAdapter.removeItem()
            openTaskAdapter.removeItem()
            dialog.dismiss()
            ProfilesActivity.updateWidgets(this)
        }
        val closeTask = dialog.findViewById<Button>(R.id.closeTask)
        closeTask.setOnClickListener {
            task.givePrice()
            family.getTask(selectedTask).setCompletedDate(LocalDate.now())
            if (!isMock){
                deleteTaskFromDatabase(family.getTask(selectedTask).getId(), TASKS)
                addTaskToDatabase(family.getTask(selectedTask), HISTORIC)
            }
            family.removeTask(selectedTask)
            dialog.dismiss()
            completeTaskAdapter.removeItem()
            ProfilesActivity.updateWidgets(this)
            Handler().postDelayed({
                showCashFlow(task.getChildName())
            }, 1000)
        }
    }

    private fun createNewTask() {
        showPopUp(R.layout.pop_up_add_task)

        val inputDescripcionTarea = dialog.findViewById<EditText>(R.id.inputDescripcionTarea)
        inputDescripcionTarea.setText("")

        val inputFechaLimite = dialog.findViewById<EditText>(R.id.inputFechaLimiteTarea)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val showCalendar = dialog.findViewById<Button>(R.id.show_calendar)
        showCalendar.setOnClickListener {
            showDatePickerDialog(inputFechaLimite)
        }

        val cbObligatoria = dialog.findViewById<CheckBox>(R.id.cbObligatoria)

        val inputRecompensa = dialog.findViewById<EditText>(R.id.recompensa)
        inputRecompensa.setText("")
        inputRecompensa.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        val tvAdviseDate = dialog.findViewById<TextView>(R.id.tv_advise_date)

        val childNames = family.getChildrenNames()
        val options = mutableListOf("Todos")
        for (childName in childNames){
            options.add(childName)
        }
        var selectedOption = ""
        val adapter = CustomSpinnerAdapter(this, options.toList())
        val asignarMiembro = dialog.findViewById<Spinner>(R.id.spinnerMembers)
        asignarMiembro.adapter = adapter
        asignarMiembro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedOption = options[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //Do nothing
            }
        }

        val cancelTask = dialog.findViewById<Button>(R.id.cancelTask)
        cancelTask.setOnClickListener {
            dialog.dismiss()
        }

        val addTask = dialog.findViewById<Button>(R.id.addTask)
        addTask.setOnClickListener {
            var allOk = true
            var fecha: LocalDate? = null
            if (inputDescripcionTarea.text.toString().isBlank()) {
                inputDescripcionTarea.error = "Es necesaria una descripciónd de la tarea"
                allOk = false
            }
            if (inputFechaLimite.text.toString().isNotBlank()) {
                if (Validator.isValidDate(inputFechaLimite.text.toString(), formatter)) {
                    fecha = LocalDate.parse(inputFechaLimite.text.toString(), formatter)
                } else {
                    allOk = false
                    tvAdviseDate.setTextColor(Color.RED)
                    tvAdviseDate.text = resources.getString(R.string.mal_formato_fecha)
                }
            }
            if (fecha != null) {
                if (fecha.isBefore(LocalDate.now())) {
                    allOk = false
                    tvAdviseDate.text = resources.getString(R.string.fecha_anterior_a_hoy)
                    tvAdviseDate.setTextColor(Color.RED)
                }
            }
            if (allOk) {
                val description = inputDescripcionTarea.text.toString()
                var recompensa = BigDecimal(0)
                if (inputRecompensa.text.toString().isNotBlank()) {
                    recompensa = inputRecompensa.text.toString().toBigDecimal()
                }
                val task =
                    Task(description, fecha, cbObligatoria.isChecked, recompensa, TaskState.OPEN)
                if (selectedOption != "Todos"){
                    task.setChild(family.getMember(selectedOption) as Child)
                    task.setAssigned(true)
                }
                family.addTask(task)
                if (!isMock){
                    addTaskToDatabase(task, TASKS)
                }
                if (task.getState() == TaskState.OPEN) {
                    openTaskAdapter.notifyNewTask()
                } else {
                    completeTaskAdapter.notifyNewTask()
                }
                ProfilesActivity.updateWidgets(this)
                dialog.dismiss()
            }
        }
    }

    /* Metodos para gestionar las propinas */
    private fun deleteAllowance(allowanceId: Int, selectedChild: String) {
        showPopUp(R.layout.pop_up_delete)
        val tvDelete = dialog.findViewById<TextView>(R.id.tvDelete)
        tvDelete.text = resources.getString(R.string.pop_up_delete_allowance)

        val cancel = dialog.findViewById<Button>(R.id.cancelDelete)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmDelete)
        confirmButton.setOnClickListener {
            val child = family.getMember(selectedChild) as Child
            if (!isMock){
                deleteAllowanceFromDatabase(child, allowanceId)
            }else{
                child.getAllowances().removeAt(allowanceId)
            }
            dialog.dismiss()
            memberAdapter.notifyDataSetChanged()
        }
    }

    private fun editAllowance(allowanceId: Int, selectedChild: String) {
        showPopUp(R.layout.pop_up_add_allowance)
        val child = family.getMember(selectedChild) as Child
        var allowance = child.getAllowances()[allowanceId]

        /*Inicialización de los elementos*/
        val inputNombreAsignacion = dialog.findViewById<EditText>(R.id.inputNombreAsignacion)
        inputNombreAsignacion.setText(allowance.getName())

        val inputDate = dialog.findViewById<EditText>(R.id.inputFechaLimite)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        inputDate.setText(allowance.getNextPayment().format(formatter))
        val showCalendar = dialog.findViewById<Button>(R.id.show_calendar)
        showCalendar.setOnClickListener {
            showDatePickerDialog(inputDate)
        }

        val inputFrecuencia = dialog.findViewById<Spinner>(R.id.botonSeleccionarFrecuencia)
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this, R.array.tipo_asignacion, R.layout.item_spinner_layout
        )
        inputFrecuencia.adapter = adapter
        var selected = 0
        when (allowance.getType()) {
            AllowanceType.PUNTUAL -> selected = 0
            AllowanceType.SEMANAL -> selected = 1
            AllowanceType.MENSUAL -> selected = 2
            AllowanceType.TRIMESTRAL -> selected = 3
            AllowanceType.SEMESTRAL -> selected = 4
            AllowanceType.ANUAL -> selected = 5
        }
        inputFrecuencia.setSelection(selected)

        val inputCantidadAsignacion = dialog.findViewById<EditText>(R.id.inputCantidadAsignacion)
        inputCantidadAsignacion.setText(allowance.getAmount().toString())
        inputCantidadAsignacion.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        /* Mensajes de errores */
        val tvErrorFecha = dialog.findViewById<TextView>(R.id.tvErrorFecha)
        val tvErrorFrecuencia = dialog.findViewById<TextView>(R.id.tvErrorFrecuencia)
        tvErrorFecha.visibility = View.GONE
        tvErrorFrecuencia.visibility = View.GONE
        /* Botones de añadir y cancelar */
        val cancelNewAllowance = dialog.findViewById<Button>(R.id.cancelNewAllowance)
        cancelNewAllowance.setOnClickListener {
            dialog.dismiss()
        }

        val addAllowance = dialog.findViewById<Button>(R.id.addAllowance)
        addAllowance.text = resources.getString(R.string.confirmar)
        addAllowance.setOnClickListener {
            var isCorrect = true
            var frecuencia: AllowanceType? = null
            var fecha: LocalDate? = null
            when (inputFrecuencia.selectedItem.toString()) {
                "Puntual" -> frecuencia = AllowanceType.PUNTUAL
                "Semanal" -> frecuencia = AllowanceType.SEMANAL
                "Mensual" -> frecuencia = AllowanceType.MENSUAL
                "Trimestral" -> frecuencia = AllowanceType.TRIMESTRAL
                "Semestral" -> frecuencia = AllowanceType.SEMESTRAL
                "Anual" -> frecuencia = AllowanceType.ANUAL
            }
            if (Validator.isValidDate(inputDate.text.toString(), formatter)) {
                fecha = LocalDate.parse(inputDate.text.toString(), formatter)
            } else {
                isCorrect = false
                tvErrorFecha.visibility = View.VISIBLE
                tvErrorFecha.text = resources.getString(R.string.mal_formato_fecha)
            }
            if (inputNombreAsignacion.text.toString().isBlank()) {
                isCorrect = false
                inputNombreAsignacion.error = "Falta un nombre para la asignación"
            }
            if (inputNombreAsignacion.text.toString().length > 20) {
                isCorrect = false
                inputNombreAsignacion.error = "Nombre demasiado largo (Max. 20)"
            }
            if (fecha != null) {
                if (fecha.isBefore(LocalDate.now())) {
                    isCorrect = false
                    tvErrorFecha.visibility = View.VISIBLE
                    tvErrorFecha.text = resources.getString(R.string.mal_formato_fecha)
                }
            }
            if (frecuencia == null) {
                isCorrect = false
                tvErrorFrecuencia.visibility = View.VISIBLE
            }
            if (inputCantidadAsignacion.text.toString().isBlank()) {
                isCorrect = false
                inputCantidadAsignacion.error = "Se necesita una cantidad"
            }
            if (isCorrect) {
                allowance.setName(inputNombreAsignacion.text.toString())
                allowance.setNextPayment(fecha!!)
                allowance.setAmount(inputCantidadAsignacion.text.toString().toBigDecimal())
                allowance.setType(frecuencia!!)
                child.updateAllowance(allowance, allowanceId)
                if (!isMock){
                    updateAllowanceInDatabase(child.getId(),allowance)
                }
                memberAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
    }

    private fun addAllowance(selectedMember: String) {
        showPopUp(R.layout.pop_up_add_allowance)
        /*Inicialización de los elementos*/
        val inputNombreAsignacion = dialog.findViewById<EditText>(R.id.inputNombreAsignacion)
        inputNombreAsignacion.setText("")

        val inputDate = dialog.findViewById<EditText>(R.id.inputFechaLimite)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        inputDate.setText(LocalDate.now().format(formatter))
        val showCalendar = dialog.findViewById<Button>(R.id.show_calendar)
        showCalendar.setOnClickListener {
            showDatePickerDialog(inputDate)
        }

        val inputFrecuencia = dialog.findViewById<Spinner>(R.id.botonSeleccionarFrecuencia)
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this, R.array.tipo_asignacion, R.layout.item_spinner_layout
        )
        inputFrecuencia.adapter = adapter

        val inputCantidadAsignacion = dialog.findViewById<EditText>(R.id.inputCantidadAsignacion)
        inputCantidadAsignacion.setText("")
        inputCantidadAsignacion.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        /* Mensajes de errores */
        val tvErrorFecha = dialog.findViewById<TextView>(R.id.tvErrorFecha)
        val tvErrorFrecuencia = dialog.findViewById<TextView>(R.id.tvErrorFrecuencia)
        tvErrorFecha.visibility = View.GONE
        tvErrorFrecuencia.visibility = View.GONE
        /* Botones de añadir y cancelar */
        val cancelNewAllowance = dialog.findViewById<Button>(R.id.cancelNewAllowance)
        cancelNewAllowance.setOnClickListener {
            dialog.dismiss()
        }

        val addAllowance = dialog.findViewById<Button>(R.id.addAllowance)
        addAllowance.setOnClickListener {
            var isCorrect = true
            var frecuencia: AllowanceType? = null
            var fecha: LocalDate? = null
            when (inputFrecuencia.selectedItem.toString()) {
                "Puntual" -> frecuencia = AllowanceType.PUNTUAL
                "Semanal" -> frecuencia = AllowanceType.SEMANAL
                "Mensual" -> frecuencia = AllowanceType.MENSUAL
                "Trimestral" -> frecuencia = AllowanceType.TRIMESTRAL
                "Semestral" -> frecuencia = AllowanceType.SEMESTRAL
                "Anual" -> frecuencia = AllowanceType.ANUAL
            }
            if (Validator.isValidDate(inputDate.text.toString(), formatter)) {
                fecha = LocalDate.parse(inputDate.text.toString(), formatter)
            } else {
                isCorrect = false
                tvErrorFecha.visibility = View.VISIBLE
                tvErrorFecha.text = resources.getString(R.string.mal_formato_fecha)
            }
            if (inputNombreAsignacion.text.toString().isBlank()) {
                isCorrect = false
                inputNombreAsignacion.error = "Falta un nombre para la asignación"
            }
            if (inputNombreAsignacion.text.toString().length > 20) {
                isCorrect = false
                inputNombreAsignacion.error = "Nombre demasiado largo (Max. 20)"
            }
            if (fecha != null) {
                if (fecha.isBefore(LocalDate.now())) {
                    isCorrect = false
                    tvErrorFecha.visibility = View.VISIBLE
                    tvErrorFecha.text = resources.getString(R.string.fecha_anterior_a_hoy)
                }
            }
            if (frecuencia == null) {
                isCorrect = false
                tvErrorFrecuencia.visibility = View.VISIBLE
            }
            if (inputCantidadAsignacion.text.toString().isBlank()) {
                isCorrect = false
                inputCantidadAsignacion.error = "Se necesita una cantidad"
            }
            if (isCorrect) {
                val child = family.getMember(selectedMember) as Child

                val allowance = Allowance(
                    inputNombreAsignacion.text.toString(),
                    fecha!!,
                    inputCantidadAsignacion.text.toString().toBigDecimal(),
                    frecuencia!!
                )
                if (!isMock) {
                    addAllowanceToDatabase(child, allowance)
                }else{
                    child.addAllowance(allowance)
                }
                memberAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
    }

    /* Metodos para gestionar los miembros de la familia */

    private fun addMember() {
        showPopUp(R.layout.pop_up_add_member)

        val memberNameWarning = dialog.findViewById<TextView>(R.id.tvMiembroExistente)
        memberNameWarning.visibility = View.INVISIBLE
        val passwordWarning = dialog.findViewById<TextView>(R.id.tv_advise_password2)
        passwordWarning.visibility = View.INVISIBLE
        val tvCantidadInicial = dialog.findViewById<TextView>(R.id.tvCantidadInicial)
        val inputCantidadInicial = dialog.findViewById<EditText>(R.id.inputCantidadInicial)
        inputCantidadInicial.setText("")
        inputCantidadInicial.filters = arrayOf(DecimalDigitsInputFilter(MAX_DECIMALS))

        var tipo = 2
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.rgMemberButtons)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = dialog.findViewById<RadioButton>(checkedId)
            tipo = selectedRadioButton.tag.toString().toInt()
            if (tipo == 2) {
                tvCantidadInicial.visibility = View.VISIBLE
                inputCantidadInicial.visibility = View.VISIBLE
            } else {
                tvCantidadInicial.visibility = View.GONE
                inputCantidadInicial.visibility = View.GONE
            }
        }
        val userName = dialog.findViewById<EditText>(R.id.inputNombreUsuario)
        userName.setText("")
        val passwordInput = dialog.findViewById<EditText>(R.id.passwordAddMember)
        passwordInput.setText("")
        val passwordInputRepeat = dialog.findViewById<EditText>(R.id.passwordAddMemberAgain)
        passwordInputRepeat.setText("")

        val showPassword = dialog.findViewById<CheckBox>(R.id.show_password)
        showPassword.setOnCheckedChangeListener { _, isChecked ->
            passwordInput.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
        val showPassword2 = dialog.findViewById<CheckBox>(R.id.show_repeat)
        showPassword2.setOnCheckedChangeListener { _, isChecked ->
            passwordInputRepeat.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        val cancel = dialog.findViewById<Button>(R.id.cancelNewMember)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val addNewMember = dialog.findViewById<Button>(R.id.addNewMember)
        addNewMember.setOnClickListener {
            memberNameWarning.visibility = View.INVISIBLE
            passwordWarning.visibility = View.INVISIBLE
            var memberName = ""
            if (userName.text.isEmpty()) {
                userName.error = "Se necesita un nombre de usuario"
            } else {
                memberName = userName.text.toString()
            }
            if (memberName != "") {
                if (family.checkName(userName.text.toString())) {
                    memberNameWarning.visibility = View.VISIBLE
                } else if (userName.text.toString().length > MAX_USER_LENGHT) {
                    userName.error = "Nombre demasiado largo. Máximo 15 caracteres."
                } else if (passwordInput.text.toString().length < 4 && passwordInput.text.toString()
                        .isNotEmpty()
                ) {
                    val tvRecomendado = dialog.findViewById<TextView>(R.id.tvRecomendado)
                    tvRecomendado.setTextColor(Color.RED)
                    tvRecomendado.text = resources.getText(R.string.password_min_size)
                } else if (passwordInput.text.toString() != passwordInputRepeat.text.toString()) {
                    passwordWarning.visibility = View.VISIBLE
                } else {
                    when (tipo) {
                        1 -> {
                            val parent = Parent(memberName, "")
                            parent.setPassword(passwordInput.text.toString())

                            if (!isMock){
                                addParentToDatabase(parent)
                            }else{
                                membersCopy[memberName] = parent
                                val result = family.addMember(parent)
                                memberAdapter.notifyDataSetChanged()
                                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                            }
                        }

                        2 -> {
                            val child = Child(memberName, "")
                            child.setPassword(passwordInput.text.toString())
                            if (inputCantidadInicial.text.toString().isNotBlank())
                                child.setActualMoney(inputCantidadInicial.text.toString().toBigDecimal())

                            if (!isMock){
                                addChildToDatabase(child)
                            }else{
                                membersCopy[memberName] = child
                                val result = family.addMember(child)
                                memberAdapter.notifyDataSetChanged()
                                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                                initSpinner()
                            }
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                "Ha ocurrido un error inesperado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    private fun deleteMember(selectedMember: String) {
        showPopUp(R.layout.pop_up_delete)

        val cancel = dialog.findViewById<Button>(R.id.cancelDelete)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmDelete)
        confirmButton.setOnClickListener {
            if (!isMock){
                deleteMemberFromDatabase(selectedMember)
            }else{
                family.deleteMember(selectedMember)
                var index = 0
                var position = 0
                for ((key, _) in membersCopy) {
                    if (selectedMember == key) {
                        position = index
                        break
                    }
                    index++
                }
                membersCopy.remove(selectedMember)
                memberAdapter.notifyItemRemoved(position)
            }
            dialog.dismiss()
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
        val tvCantidadInicial = dialog.findViewById<TextView>(R.id.tvCantidadInicial)
        tvCantidadInicial.visibility = View.GONE
        val inputCantidadInicial = dialog.findViewById<EditText>(R.id.inputCantidadInicial)
        inputCantidadInicial.visibility = View.GONE

        val userName = dialog.findViewById<EditText>(R.id.inputNombreUsuario)
        userName.setText(member.getUser())
        val passwordInput = dialog.findViewById<EditText>(R.id.passwordAddMember)
        passwordInput.setText("")
        val passwordInputRepeat = dialog.findViewById<EditText>(R.id.passwordAddMemberAgain)
        passwordInputRepeat.setText("")

        val showPassword = dialog.findViewById<CheckBox>(R.id.show_password)
        showPassword.setOnCheckedChangeListener { _, isChecked ->
            passwordInput.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
        val showPassword2 = dialog.findViewById<CheckBox>(R.id.show_repeat)
        showPassword2.setOnCheckedChangeListener { _, isChecked ->
            passwordInputRepeat.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

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
            if (userName.text.isEmpty()) {
                userName.error = "Se necesita un nombre de usuario"
            } else {
                memberName = userName.text.toString()
            }
            if (memberName != "") {
                if (family.checkName(userName.text.toString()) && userName.text.toString() != selectedMember) {
                    memberNameWarning.visibility = View.VISIBLE
                } else if (userName.text.toString().length > MAX_USER_LENGHT) {
                    userName.error = "Nombre demasiado largo. Máximo 10 caracteres."
                } else if (passwordInput.text.toString().length < 4 && passwordInput.text.toString()
                        .isNotEmpty()) {
                    val tvRecomendado = dialog.findViewById<TextView>(R.id.tvRecomendado)
                    tvRecomendado.setTextColor(Color.RED)
                    tvRecomendado.text = resources.getText(R.string.password_min_size)
                } else if (passwordInput.text.toString() != passwordInputRepeat.text.toString()) {
                    passwordWarning.visibility = View.VISIBLE
                } else {
                    family.deleteMember(selectedMember)
                    membersCopy.remove(selectedMember)

                    member.setUser(memberName)
                    member.setPassword(passwordInput.text.toString())
                    family.addMember(member)
                    membersCopy[memberName] = member

                    if (!isMock) updateMemberInDatabase(member)

                    dialog.dismiss()
                    memberAdapter.notifyDataSetChanged()
                }
            }

        }
    }

    /* Metodos para inicializar los RecycleView */
    private fun showCashFlow(child: String) {

        val childSelected = family.getMember(child) as Child
        val recyclerView = findViewById<RecyclerView>(R.id.rvCashFlow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (childSelected.getCashFlow().isNotEmpty()) {
            recyclerView.adapter = ExpenseAdapter(childSelected.getCashFlow())
        } else {
            Toast.makeText(this, "No existen movimientos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMembers() {
        membersCopy = family.getMembers().toMutableMap()
        membersCopy.remove(user)
        memberAdapter = MemberAdapter(
            memberMap = membersCopy,
            editMember = { selectedMember -> editMember(selectedMember) },
            deleteMember = { selectedMember -> deleteMember(selectedMember) },
            addAllowance = { selectedMember -> addAllowance(selectedMember) },
            editAllowance = { allowanceId, selectedChild ->
                editAllowance(
                    allowanceId,
                    selectedChild
                )
            },
            deleteAllowance = { allowanceId, selectedChild ->
                deleteAllowance(
                    allowanceId,
                    selectedChild
                )
            },
            this
        )
        val recyclerView = findViewById<RecyclerView>(R.id.rvAdminFamilia)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (family.getMembers().isNotEmpty()) {
            recyclerView.adapter = memberAdapter
        } else {
            //TODO cerrar aplicacion y borrar familia
        }
    }

    private fun showTasks() {
        openTaskAdapter = TaskOpenAdapter(
            tasks = family.getTaskList()
        ) { selectedTask ->
            closeTask(
                selectedTask
            )
        }
        rvTaskPendientes.layoutManager = LinearLayoutManager(this)
        rvTaskPendientes.adapter = openTaskAdapter

        completeTaskAdapter = TaskCompleteAdapter(
            tasks = family.getTaskList()
        ) { selectedTask ->
            completeTask(
                selectedTask,
            )
        }
        rvTaskCompletadas.layoutManager = LinearLayoutManager(this)
        rvTaskCompletadas.adapter = completeTaskAdapter
    }

    /* Metodos para animar la interfaz */

    private fun animateAdministracion() {
        if (tareas) {
            Animations.animateViewOfFloat(taskLayout, "translationX", -2000f, 300)
            //taskLayout.visibility = View.GONE
        }
        if (seguimiento) {
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
        if (seguimiento) {
            Animations.animateViewOfFloat(seguimientoLayout, "translationX", -2000f, 300)
            //seguimientoLayout.visibility = View.GONE
            taskLayout.translationX = 2000f
        }
        if (administracion) {
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
        if (tareas) {
            Animations.animateViewOfFloat(taskLayout, "translationX", 2000f, 300)
            //taskLayout.visibility = View.GONE
        }
        if (administracion) {
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

    /* Elementos emergentes o pop ups */

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showDatePickerDialog(inputDate: EditText) {
        val newFragment =
            DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
                var dayString = ""
                var monthString = ""
                val yearString = year.toString()
                dayString = if (day.toString().toInt() < 10) {
                    "0$day"
                } else {
                    day.toString()
                }
                monthString = if (month.toString().toInt() + 1 < 10) {
                    "0${month.toString().toInt() + 1}"
                } else {
                    "${month.toString().toInt() + 1}"
                }
                val selectedDate = "$dayString/$monthString/$yearString"
                inputDate.setText(selectedDate)
            })

        newFragment.show(supportFragmentManager, "datePicker")
    }

    /* Metodos de control del activity */

    private fun initToolBar() {
        toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.main_parents)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.bar_tittle,
            R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toggle)

        toggle.syncState()

        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                drawer.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun initNavView() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val headerView: View =
            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

        val tvUser: TextView = headerView.findViewById(R.id.tvUserEmail)
        tvUser.text = family.getEmail()
        tvUser.setTextColor(Color.BLACK)
    }

    @Deprecated("Deprecated")
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (ProfileFragment.configProfileOpen) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentProfile)
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            ProfileFragment.configProfileOpen = false
        }else if (TaskHistoryFragment.configProfileOpen) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentProfile)
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            TaskHistoryFragment.configProfileOpen = false
        }else if (GraphFragment.configProfileOpen) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentProfile)
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            GraphFragment.configProfileOpen = false
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
            R.id.nav_item_record -> showTaskHistory()
            R.id.nav_item_signout -> signOut()
            R.id.nav_item_backprofiles -> backProfiles()
            R.id.nav_item_share -> share()
            R.id.nav_item_chart -> showGraphs()
            R.id.nav_item_notifications -> notificationSettings()
        }
        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun notificationSettings() {
        showPopUp(R.layout.pop_up_notification_preferences)
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)

        val backNotification = dialog.findViewById<ImageView>(R.id.backNotification)
        backNotification.setOnClickListener {
            dialog.dismiss()
        }

        val switchEmailNotification = dialog.findViewById<SwitchCompat>(R.id.switchEmailNotification)
        switchEmailNotification.isChecked = sharedPreferences.getBoolean(Constants.EMAIL_NOTIFICATIONS, false)
        switchEmailNotification.setOnClickListener {
            sharedPreferences.edit()
                .putBoolean(Constants.EMAIL_NOTIFICATIONS,switchEmailNotification.isChecked)
                .apply()
        }

        val switchPushNotification = dialog.findViewById<SwitchCompat>(R.id.switchPushNotification)
        switchPushNotification.isChecked = sharedPreferences.getBoolean(Constants.PUSH_NOTIFICATIONS, false)
        switchPushNotification.setOnClickListener {
            sharedPreferences.edit()
                .putBoolean(Constants.PUSH_NOTIFICATIONS,switchPushNotification.isChecked)
                .apply()
        }
    }

    private fun showGraphs() {
        val fragmentView = findViewById<FragmentContainerView>(R.id.fragmentProfile)
        val bundle = bundleOf()
        GraphFragment.configProfileOpen = true
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<GraphFragment>(R.id.fragmentProfile, args = bundle)
        }
        fragmentView.translationX = 500f
        Animations.animateViewOfFloat(fragmentView, "translationX", 0f, 300)
    }

    private fun showTaskHistory() {
        val fragmentView = findViewById<FragmentContainerView>(R.id.fragmentProfile)
        val bundle = bundleOf()
        TaskHistoryFragment.configProfileOpen = true
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<TaskHistoryFragment>(R.id.fragmentProfile, args = bundle)
        }
        fragmentView.translationX = 500f
        Animations.animateViewOfFloat(fragmentView, "translationX", 0f, 300)
    }

    private fun showEditProfile() {
        val fragmentView = findViewById<FragmentContainerView>(R.id.fragmentProfile)
        val bundle = bundleOf(USER_BUNDLE to user)
        ProfileFragment.configProfileOpen = true
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<ProfileFragment>(R.id.fragmentProfile, args = bundle)
        }
        fragmentView.translationX = 500f
        Animations.animateViewOfFloat(fragmentView, "translationX", 0f, 300)
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

    private fun initSpinner() {
        val options = family.getChildrenNames()
        val adapter = CustomSpinnerAdapter(this, options)
        seleccionarMiembro.adapter = adapter
        seleccionarMiembro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
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

    /* Operaciones de Firebase */

    private fun updateMemberInDatabase(member: Member) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(member.getId())
            .update(
                mapOf(
                    "user" to member.getUser(),
                    "password" to member.getPassword(),
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this,"Datos del miembro actualizados correctamente", Toast.LENGTH_SHORT).show()
                memberAdapter.notifyDataSetChanged()
                println("Datos del miembro actualizados correctamente")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"Error al actualizar datos del miembro", Toast.LENGTH_SHORT).show()
                println("Error al actualizar datos del padre: $e")
            }
    }

    private fun addAllowanceToDatabase(child: Child, allowance: Allowance) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getId())
            .collection(ALLOWANCES)
            .add(
                hashMapOf(
                    "name" to allowance.getName(),
                    "nextPayment" to allowance.getNextPayment().format(dateFormat),
                    "amount" to allowance.getAmount().toString(),
                    "type" to AllowanceType.toString(allowance.getType()),
                    "expired" to allowance.allowanceExpired()
                )
            )
            .addOnSuccessListener { document ->
                allowance.setId(document.id)
                child.addAllowance(allowance)
                memberAdapter.notifyDataSetChanged()
                Toast.makeText(this,"Asignacion añadida", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Toast.makeText(this,"Error al crear la asignación", Toast.LENGTH_SHORT).show()
                println("Error al crear asignación: $e")
            }
    }

    private fun updateAllowanceInDatabase(childId: String, allowance: Allowance) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(childId)
            .collection(ALLOWANCES)
            .document(allowance.getId())
            .update(
                mapOf(
                    "name" to allowance.getName(),
                    "nextPayment" to allowance.getNextPayment().format(dateFormat),
                    "amount" to allowance.getAmount().toString(),
                    "type" to AllowanceType.toString(allowance.getType()),
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this,"Asignación actualizada correctamente",Toast.LENGTH_SHORT).show()
                memberAdapter.notifyDataSetChanged()
                println("Asignación actualizada correctamente")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"Error al actualizar asignación",Toast.LENGTH_SHORT).show()
                println("Error al actualizar asignación: $e")
            }
    }

    private fun deleteAllowanceFromDatabase(child: Child, allowanceId: Int) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getId())
            .collection(ALLOWANCES)
            .document(child.getAllowances()[allowanceId].getId())
            .delete()
            .addOnSuccessListener {
                child.getAllowances().removeAt(allowanceId)
                memberAdapter.notifyDataSetChanged()
                Toast.makeText(this,"Asignación eliminadada correctamente",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error al eliminar la asignación",Toast.LENGTH_SHORT).show()
                println("Error al eliminar la asignación")
            }
    }

    private fun addParentToDatabase(parent: Parent) {
        if (family.getMembers().size <= 12){
            FirebaseFirestore.getInstance()
                .collection(uid)
                .document(FAMILY)
                .collection(MEMBERS)
                .add(
                    hashMapOf(
                        "user" to parent.getUser(),
                        "password" to parent.getPassword(),
                        "type" to MemberType.toString(MemberType.PARENT)
                    )
                )
                .addOnSuccessListener { document ->
                    membersCopy[parent.getUser()] = parent
                    val result = family.addMember(parent)
                    parent.setId(document.id)
                    memberAdapter.notifyDataSetChanged()
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Se producjo un error al crear al miembro", Toast.LENGTH_SHORT).show()
                    println("Error al crear al miembro")
                }
        }
    }

    private fun addChildToDatabase(child: Child) {
        if (family.getMembers().size <= 12){
            FirebaseFirestore.getInstance()
                .collection(uid)
                .document(FAMILY)
                .collection(MEMBERS)
                .add(
                    hashMapOf(
                        "user" to child.getUser(),
                        "password" to child.getPassword(),
                        "money" to child.getActualMoney().toString(),
                        "type" to MemberType.toString(MemberType.CHILD)
                    )
                )
                .addOnSuccessListener { document ->
                    membersCopy[child.getUser()] = child
                    val result = family.addMember(child)
                    child.setId(document.id)
                    memberAdapter.notifyDataSetChanged()
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                    initSpinner()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear al miembro", Toast.LENGTH_SHORT).show()
                    println("Error al crear al miembro")
                }
        }
    }

    private fun deleteMemberFromDatabase(selectedMember: String) {
        val userId = family.getMember(selectedMember)!!.getId()
        //Si es un menor borramos todos sus datos
        if (!family.isParent(selectedMember)){
            deleteAllAllowancesFromDatabase(userId)
            deleteCashFlowFromDatabase(userId)
            deleteAllGoalsFromDatabase(userId)
            for (task in family.getTaskOfChild(selectedMember)){
                if (task.isAssigned()){
                    deleteTaskFromDatabase(task.getId(), TASKS)
                }
            }
            for (task in family.getTaskHistory()){
                if (task.isAssigned() && task.getChild()!!.getUser() == selectedMember){
                    deleteTaskFromDatabase(task.getId(), HISTORIC)
                }
            }
        }
        //Eliminamos las fotos de perfil (si tiene)
        deletePictures(userId)

        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(userId)
            .delete()
            .addOnSuccessListener {
                family.deleteMember(selectedMember)
                var index = 0
                var position = 0
                for ((key, _) in membersCopy) {
                    if (selectedMember == key) {
                        position = index
                        break
                    }
                    index++
                }
                membersCopy.remove(selectedMember)
                memberAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Miembro eliminado correctamente", Toast.LENGTH_SHORT).show()
                println("Miembro eliminado correctamente")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar al miembro", Toast.LENGTH_SHORT).show()
                println("Error al eliminar al miembro")
            }
    }

    private fun deleteAllAllowancesFromDatabase(userId: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(userId)
            .collection(ALLOWANCES)
            .get()
            .addOnSuccessListener { allowances ->
                for (allowance in allowances){
                    allowance.reference.delete()
                }
            }
    }

    private fun deleteCashFlowFromDatabase(userId: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(userId)
            .collection(CASHFLOW)
            .get()
            .addOnSuccessListener { moves ->
                for (move in moves){
                    move.reference.delete()
                }
            }
    }

    private fun deleteAllGoalsFromDatabase(userId: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(MEMBERS)
            .document(userId)
            .collection(GOALS)
            .get()
            .addOnSuccessListener { goals ->
                for (goal in goals){
                    goal.reference.delete()
                }
            }
    }

    private fun addTaskToDatabase(task: Task, typeOfTask: String) {
        var limitDate = ""
        if (task.getLimitDate() != null) {
            limitDate = task.getLimitDate()!!.format(dateFormat)
        }
        var completedDate = ""
        if (typeOfTask == HISTORIC && task.getCompletedDate() != null) {
            completedDate = task.getCompletedDate()!!.format(dateFormat)
        }
        var childId = ""
        if (task.getChild() != null){
            childId = task.getChild()!!.getId()
        }
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(typeOfTask)
            .add(
                hashMapOf(
                    "description" to task.getDescription(),
                    "limitDate" to limitDate,
                    "mandatory" to task.isMandatory(),
                    "price" to task.getPrice().toString(),
                    "state" to TaskState.toString(task.getState()),
                    "completedDate" to completedDate,
                    "child" to childId,
                    "assigned" to task.isAssigned()
                )
            )
            .addOnSuccessListener { document ->
                task.setId(document.id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al añadir la tarea", Toast.LENGTH_SHORT).show()
                println("Error al añadir la tarea")
            }
    }

    private fun deleteTaskFromDatabase(id: String, typeOfTask: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(typeOfTask)
            .document(id)
            .delete()
            .addOnSuccessListener {
                println("Tarea eliminada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar la tarea: $e")
            }
    }

    private fun updateTaskInDatabase(task: Task, typeOfTask: String) {
        var childId = ""
        if (task.getChild() != null){
            childId = task.getChild()!!.getId()
        }
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(FAMILY)
            .collection(typeOfTask)
            .document(task.getId())
            .update(
                mapOf(
                    "state" to TaskState.toString(task.getState()),
                    "completedDate" to "",
                    "child" to childId
                )
            )
            .addOnSuccessListener {
                println("Documento actualizado exitosamente.")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar documento: $e")
            }
    }

    private fun deletePictures(userId: String){
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$userId")
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("FirebaseStorage", "Archivo eliminado exitosamente.")
            }
            .addOnFailureListener{exc ->
                Log.e("FirebaseStorage", "Error al eliminar el archivo", exc)
            }
    }

    /* Metodos de control de gestos */

    override fun onDown(e: MotionEvent): Boolean {
        //do nothing
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        //do nothing
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        //do nothing
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //do nothing
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        //do nothing
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val swipe = 100
        val swipeVelocity = 100

        if (e1 != null) {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipe && abs(velocityX) > swipeVelocity) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun onSwipeLeft() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            if (seguimiento){
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }else if (tareas){
                restartButtons()
                changeButtonState(adminButton)
                animateAdministracion()
            }
        }else{
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    private fun onSwipeRight() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            if (administracion){
                restartButtons()
                changeButtonState(taskButton)
                animateTareas()
            }else if (tareas){
                restartButtons()
                changeButtonState(seguimientoButton)
                animateSegimiento()
            }else if (seguimiento){
                drawer.openDrawer(GravityCompat.START)
            }
        }
    }
}