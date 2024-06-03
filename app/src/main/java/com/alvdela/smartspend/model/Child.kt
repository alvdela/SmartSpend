package com.alvdela.smartspend.model

import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.time.LocalDate

class Child(user: String, password: String) : Member(user, password) {

    private var actualMoney: BigDecimal = BigDecimal(0)
    private var allowanceList: MutableList<Allowance> = mutableListOf()
    private var cashFlowList: MutableList<CashFlow> = mutableListOf()
    private var goalList: MutableList<SavingGoal> = mutableListOf()

    fun getActualMoney(): BigDecimal {
        return this.actualMoney
    }

    fun setActualMoney(money: BigDecimal) {
        this.actualMoney = money
    }

    fun addAllowance(allowance: Allowance) {
        this.allowanceList.add(allowance)
    }

    fun updateAllowance(allowance: Allowance, id: Int) {
        this.allowanceList.removeAt(id)
        this.allowanceList.add(id, allowance)
    }

    fun getAllowances(): MutableList<Allowance> {
        return allowanceList
    }

    fun setAllowances(allowances: MutableList<Allowance>){
        this.allowanceList = allowances
    }

    fun getPayment() {
        val iterator = allowanceList.iterator()
        while (iterator.hasNext()) {
            val allowance = iterator.next()
            if (allowance.checkPaymentDay()) {
                val payment = CashFlow(
                    allowance.getName(),
                    allowance.getAmount(),
                    CashFlowType.INGRESO,
                    LocalDate.now()
                )
                addIncome(payment)
                if (!ContextFamily.isMock){
                    updateAllowanceInDatabase(allowance)
                    updateMoneyInDatabase(true, allowance.getPayment())
                }else{
                    this.actualMoney += allowance.getPayment()
                }
            }
            if (allowance.allowanceExpired()) {
                if(!ContextFamily.isMock)
                    deleteAllowanceFromDatabase(allowance.getId())
                iterator.remove()
            }
        }

    }

    fun addExpense(cashFlow: CashFlow): Int {
        if (cashFlow.amount.compareTo(getActualMoney()) == -1 || cashFlow.amount.compareTo(getActualMoney()) == 0) {
            var index = 0
            while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
                index++
            }
            if(!ContextFamily.isMock){
                addCashFlowToDatabase(index, cashFlow)
                updateMoneyInDatabase(false, cashFlow.amount)
            }else{
                actualMoney -= cashFlow.amount
                this.cashFlowList.add(index, cashFlow)
            }
            return index
        }
        return -1
    }

    private fun addIncome(cashFlow: CashFlow) {
        var index = 0
        while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
            index++
        }
        if(!ContextFamily.isMock){
            addCashFlowToDatabase(index,cashFlow)
        }else{
            this.cashFlowList.add(index, cashFlow)
        }
    }

    fun getCashFlow(): MutableList<CashFlow> {
        return cashFlowList
    }

    fun setCashFlow(cashFlowList: MutableList<CashFlow>){
        this.cashFlowList = cashFlowList
    }

    fun addGoal(goal: SavingGoal) {
        this.goalList.add(goal)
    }

    fun getGoals(): MutableList<SavingGoal> {
        return this.goalList
    }

    fun setGoals(goals: MutableList<SavingGoal>){
        this.goalList = goals
    }

    fun claimGoal(i: Int) {
        val goal = getGoals()[i]
        if (goal.getSaving().compareTo(BigDecimal(0)) != 0){
            val payment =
                CashFlow(goal.getDescription(), goal.getSaving(), CashFlowType.INGRESO, LocalDate.now())
            addIncome(payment)
            if (!ContextFamily.isMock){
                updateMoneyInDatabase(true, goal.getSaving())
            }else{
                this.actualMoney += goal.getSaving()
            }
        }
        this.goalList.removeAt(i)
    }

    fun claimPrice(description: String, money: BigDecimal) {
        val payment = CashFlow(description, money, CashFlowType.RECOMPENSA, LocalDate.now())
        addIncome(payment)
        if (!ContextFamily.isMock){
            updateMoneyInDatabase(true, money)
        }else{
            this.actualMoney += money
        }
    }

    private fun deleteAllowanceFromDatabase(allowanceId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirebaseAuth.getInstance().currentUser!!.uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(getId())
            .collection(Constants.ALLOWANCES)
            .document(allowanceId)
            .delete()
            .addOnSuccessListener {
                println("Documento eliminado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar documento: $e")
            }
    }

    private fun addCashFlowToDatabase(index: Int, cashFlow: CashFlow) {
        FirebaseFirestore.getInstance()
            .collection(FirebaseAuth.getInstance().currentUser!!.uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(getId())
            .collection(Constants.CASHFLOW)
            .add(
                hashMapOf(
                    "description" to cashFlow.description,
                    "amount" to cashFlow.amount.toString(),
                    "type" to CashFlowType.toString(cashFlow.type),
                    "date" to cashFlow.date.format(Constants.dateFormat)
                )
            )
            .addOnSuccessListener {
                this.cashFlowList.add(index, cashFlow)
                println("Movimiento añadido correctamente")
            }
            .addOnFailureListener {
                println("Error al añadir el movimiento")
            }
    }

    private fun updateAllowanceInDatabase(allowance: Allowance) {
        FirebaseFirestore.getInstance()
            .collection(FirebaseAuth.getInstance().currentUser!!.uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(getId())
            .collection(Constants.ALLOWANCES)
            .document(allowance.getId())
            .update(
                mapOf(
                    "nextPayment" to allowance.getNextPayment().format(Constants.dateFormat)
                )
            )
            .addOnSuccessListener {
                println("Asignación actualizada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar asignación: $e")
            }
    }

    private fun updateMoneyInDatabase(positive: Boolean, money: BigDecimal) {
        var actualMoney: BigDecimal
        if (positive){
            actualMoney = this.actualMoney + money
        }else{
            actualMoney = this.actualMoney - money
        }
        FirebaseFirestore.getInstance()
            .collection(FirebaseAuth.getInstance().currentUser!!.uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(getId())
            .update(
                mapOf(
                    "money" to actualMoney.toString()
                )
            )
            .addOnSuccessListener {
                if (positive){
                    this.actualMoney += money
                }else{
                    this.actualMoney -= money
                }
                println("Dinero actualizado correctamente")
            }
            .addOnFailureListener {
                println("Error al actualizar el dinero disponible")
            }
    }
}