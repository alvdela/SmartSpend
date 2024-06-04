package com.alvdela.smartspend.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.CashFlowType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.ui.Animations
import com.alvdela.smartspend.adapter.CustomSpinnerAdapter
import com.echo.holographlibrary.Bar
import com.echo.holographlibrary.BarGraph
import com.echo.holographlibrary.PieGraph
import com.echo.holographlibrary.PieSlice
import java.math.BigDecimal
import java.time.Month
import kotlin.math.abs


class GraphFragment : Fragment(), GestureDetector.OnGestureListener {

    private lateinit var family: Family

    private lateinit var seleccionarMiembro: Spinner
    private lateinit var tvFood: TextView
    private lateinit var tvOcio: TextView
    private lateinit var tvCompras: TextView
    private lateinit var tvOtros: TextView
    private lateinit var pieGraphExpenses: PieGraph
    private lateinit var barGraphExpenses: BarGraph
    private lateinit var clExpenseNumbers: ConstraintLayout
    private lateinit var pieGraphButton: ImageView
    private lateinit var barGraphButton: ImageView

    private var interfaceFlag = true

    private lateinit var gestureDetector: GestureDetector

    companion object {
        var configProfileOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        family = ContextFamily.family!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObjects(view)
        val toolbarProfile = view.findViewById<Toolbar>(R.id.toolbar_profile)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbarProfile)
        toolbarProfile.setNavigationOnClickListener {
            // Cerrar el Fragment
            ProfileFragment.configProfileOpen = false
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    private fun initObjects(view: View) {
        seleccionarMiembro = view.findViewById(R.id.spinnerMembers)
        clExpenseNumbers = view.findViewById(R.id.clExpenseNumbers)
        tvFood = view.findViewById(R.id.tvFood)
        tvOcio = view.findViewById(R.id.tvOcio)
        tvCompras = view.findViewById(R.id.tvCompras)
        tvOtros = view.findViewById(R.id.tvOtros)
        pieGraphExpenses = view.findViewById(R.id.pieGraphExpenses)
        barGraphExpenses = view.findViewById(R.id.barGraphExpenses)

        pieGraphButton = view.findViewById<ImageView>(R.id.pieGraphButton)
        changeButtonState(pieGraphButton, true)
        barGraphButton = view.findViewById<ImageView>(R.id.barGraphButton)

        pieGraphButton.setOnClickListener {
            if (!interfaceFlag) {
                animatePieGraph()
                changeButtonState(pieGraphButton, true)
                changeButtonState(barGraphButton, false)
                interfaceFlag = true
            }
        }

        barGraphButton.setOnClickListener {
            if (interfaceFlag) {
                animateBarGraph()
                changeButtonState(pieGraphButton, false)
                changeButtonState(barGraphButton, true)
                interfaceFlag = false
            }
        }
        initSpinner()
        initGestures(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestures(view: View) {
        gestureDetector = GestureDetector(requireContext(), this)
        val mainView = view.findViewById<View>(R.id.fragmentGraph)
        mainView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun animatePieGraph() {
        interfaceFlag = true
        Animations.animateViewOfFloat(barGraphExpenses, "translationX", 2000f, 300)
        Animations.animateViewOfFloat(clExpenseNumbers, "translationX", 0f, 300)
        Animations.animateViewOfFloat(pieGraphExpenses, "translationX", 0f, 300)
    }

    private fun animateBarGraph() {
        interfaceFlag = true
        Animations.animateViewOfFloat(barGraphExpenses, "translationX", 0f, 300)
        Animations.animateViewOfFloat(clExpenseNumbers, "translationX", -2000f, 300)
        Animations.animateViewOfFloat(pieGraphExpenses, "translationX", -2000f, 300)
    }

    private fun changeButtonState(button: ImageView, active: Boolean) {
        if (active) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
            button.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mid_gray))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_blue))
            button.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun initSpinner() {
        val options = family.getChildrenNames()
        val adapter = CustomSpinnerAdapter(requireContext(), options)
        seleccionarMiembro.adapter = adapter
        seleccionarMiembro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOption = options[position]
                if (selectedOption != "") {
                    makePieGraph(selectedOption)
                    makeBarGraph(selectedOption)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //Do nothing
            }
        }
    }

    private fun makePieGraph(childName: String) {
        val child = family.getMember(childName)!! as Child
        var comida = BigDecimal(0)
        var ocio = BigDecimal(0)
        var compras = BigDecimal(0)
        var otros = BigDecimal(0)
        for (cashFlow in child.getCashFlow()) {
            when (cashFlow.type) {
                CashFlowType.COMIDA -> comida += cashFlow.amount
                CashFlowType.OCIO -> ocio += cashFlow.amount
                CashFlowType.COMPRAS -> compras += cashFlow.amount
                CashFlowType.OTROS -> otros += cashFlow.amount
                CashFlowType.INGRESO -> {}
                CashFlowType.RECOMPENSA -> {}
            }
        }
        val comidaSlice = PieSlice()
        comidaSlice.color = resources.getColor(R.color.color2)
        comidaSlice.value = comida.toFloat()
        pieGraphExpenses.addSlice(comidaSlice)

        val ocioSlice = PieSlice()
        ocioSlice.color = resources.getColor(R.color.color3)
        ocioSlice.value = ocio.toFloat()
        pieGraphExpenses.addSlice(ocioSlice)

        val comprasSlice = PieSlice()
        comprasSlice.color = resources.getColor(R.color.color4)
        comprasSlice.value = compras.toFloat()
        pieGraphExpenses.addSlice(comprasSlice)

        val otrosSlice = PieSlice()
        otrosSlice.color = resources.getColor(R.color.color7)
        otrosSlice.value = otros.toFloat()
        pieGraphExpenses.addSlice(otrosSlice)

        tvFood.text = "Comida: $comida€"
        tvOcio.text = "Ocio: $ocio€"
        tvCompras.text = "Compras: $compras€"
        tvOtros.text = "Otros: $otros€"
    }

    private fun makeBarGraph(childName: String) {
        val child = family.getMember(childName)!! as Child

        var monthExpense = BigDecimal(0)
        val expenses = mutableListOf<BigDecimal>()

        var month = child.getCashFlow()[0].date.month
        val months = mutableListOf<Month>()
        months.add(month)

        for (cashFlow in child.getCashFlow()) {
            if (cashFlow.date.month == month) {
                monthExpense += cashFlow.amount
            } else {
                expenses.add(0, monthExpense)
                if (expenses.size >= 6) {
                    break
                }

                monthExpense = BigDecimal(0)
                monthExpense += cashFlow.amount

                month = cashFlow.date.month
                months.add(0, month)
            }
        }
        expenses.add(0, monthExpense)

        val expensesGraph = ArrayList<Bar>()
        for ((index, expense) in expenses.withIndex()) {
            val bar = Bar()
            bar.color = Color.parseColor("#6887e1")
            when (months[index]) {
                Month.JANUARY -> bar.name = "Enero"
                Month.FEBRUARY -> bar.name = "Febrero"
                Month.MARCH -> bar.name = "Marzo"
                Month.APRIL -> bar.name = "Abril"
                Month.MAY -> bar.name = "Mayo"
                Month.JUNE -> bar.name = "Junio"
                Month.JULY -> bar.name = "Julio"
                Month.AUGUST -> bar.name = "Agosto"
                Month.SEPTEMBER -> bar.name = "Septiembre"
                Month.OCTOBER -> bar.name = "Octubre"
                Month.NOVEMBER -> bar.name = "Noviembre"
                Month.DECEMBER -> bar.name = "Diciembre"
            }
            bar.value = expense.toFloat()
            expensesGraph.add(bar)
        }
        barGraphExpenses.setBars(expensesGraph)
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
        if (interfaceFlag) {
            animateBarGraph()
            changeButtonState(pieGraphButton, false)
            changeButtonState(barGraphButton, true)
            interfaceFlag = false
        }
    }

    private fun onSwipeRight() {
        if (!interfaceFlag) {
            animatePieGraph()
            changeButtonState(pieGraphButton, true)
            changeButtonState(barGraphButton, false)
            interfaceFlag = true
        }
    }
}