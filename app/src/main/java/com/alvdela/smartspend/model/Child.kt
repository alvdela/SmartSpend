package com.alvdela.smartspend.model

import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.firebase.Constants
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
                actualMoney += allowance.getPayment()

                if (!ContextFamily.isMock){
                    updateAllowanceInDatabase(getUser(),allowance)
                }
            }
            if (allowance.allowanceExpired()) {
                if(!ContextFamily.isMock)
                    deleteAllowanceFromDatabase(this.getUser(), allowance.getId())
                iterator.remove()
            }
        }

    }

    fun addExpense(cashFlow: CashFlow): Int {
        if (cashFlow.amount.compareTo(getActualMoney()) == -1) {
            actualMoney -= cashFlow.amount
            var index = 0
            while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
                index++
            }
            this.cashFlowList.add(index, cashFlow)
            return index
        }
        return -1
    }

    private fun addIncome(cashFlow: CashFlow) {
        var index = 0
        while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
            index++
        }
        if(!ContextFamily.isMock)
            addCashFlowToDatabase(this.getUser(),cashFlow)
        this.cashFlowList.add(index, cashFlow)
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
            this.actualMoney += goal.getSaving()
        }
        this.goalList.removeAt(i)
    }

    fun claimPrice(description: String, money: BigDecimal) {
        val payment = CashFlow(description, money, CashFlowType.RECOMPENSA, LocalDate.now())
        addIncome(payment)
        this.actualMoney += money
        if(!ContextFamily.isMock)
            updateMoneyInDatabase()
    }

    private fun deleteAllowanceFromDatabase(child: String, id: String) {
        FirebaseFirestore.getInstance()
            .collection(ContextFamily.family!!.getEmail())
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(child)
            .collection(Constants.ALLOWANCES)
            .document(id)
            .delete()
            .addOnSuccessListener {
                println("Documento eliminado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar documento: $e")
            }
    }

    private fun addCashFlowToDatabase(child: String, cashFlow: CashFlow) {
        FirebaseFirestore.getInstance()
            .collection(ContextFamily.family!!.getEmail())
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(child)
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
                println("Documento añadido correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al añadir documento: $e")
            }
    }

    private fun updateAllowanceInDatabase(child: String, allowance: Allowance) {
        FirebaseFirestore.getInstance()
            .collection(ContextFamily.family!!.getEmail())
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(child)
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

    private fun updateMoneyInDatabase(){
        FirebaseFirestore.getInstance()
            .collection(ContextFamily.family!!.getEmail())
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(getUser())
            .update(
                mapOf(
                    "money" to getActualMoney().toString()
                )
            )
            .addOnSuccessListener {
                println("Dinero actualizado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar el dinero disponible: $e")
            }
    }
}