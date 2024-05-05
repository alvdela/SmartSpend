package com.alvdela.smartspend.model

import java.time.LocalDate

class Child(user: String, password: String) : Member(user, password) {

    private var actualMoney: Float = 0F
    private var allowanceList: MutableList<Allowance> = mutableListOf()
    private var cashFlowList: MutableList<CashFlow> = mutableListOf()
    private var goalList: MutableList<SavingGoal> = mutableListOf()

    fun getActualMoney(): Float {
        return this.actualMoney
    }

    fun setActualMoney(money: Float) {
        this.actualMoney = money
    }

    fun addAllowance(allowance: Allowance) {
        this.allowanceList.add(allowance)
        //TODO add to the database
    }

    fun updateAllowance(allowance: Allowance, id: Int) {
        this.allowanceList.removeAt(id)
        this.allowanceList.add(id, allowance)
        //TODO update from database
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
            }
            if (allowance.allowanceExpired()) {
                iterator.remove()
            }
        }

    }

    fun addExpense(cashFlow: CashFlow): Int {
        if (cashFlow.amount <= getActualMoney()) {
            actualMoney -= cashFlow.amount
            var index = 0
            while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
                index++
            }
            this.cashFlowList.add(index, cashFlow)
            //TODO add to the database
            return index
        }
        return -1
    }

    private fun addIncome(cashFlow: CashFlow) {
        var index = 0
        while (index < this.cashFlowList.size && this.cashFlowList[index].date.isAfter(cashFlow.date)) {
            index++
        }
        this.cashFlowList.add(index, cashFlow)
        //TODO add to the database
    }

    fun getCashFlow(): MutableList<CashFlow> {
        return cashFlowList
    }

    fun setCashFlow(cashFlowList: MutableList<CashFlow>){
        this.cashFlowList = cashFlowList
    }

    fun addGoal(goal: SavingGoal) {
        this.goalList.add(goal)
        //TODO add to the database
    }

    fun getGoals(): MutableList<SavingGoal> {
        return this.goalList
    }

    fun setGoals(goals: MutableList<SavingGoal>){
        this.goalList = goals
    }

    fun claimGoal(i: Int) {
        val goal = getGoals()[i]
        val payment =
            CashFlow(goal.getDescription(), goal.getSaving(), CashFlowType.INGRESO, LocalDate.now())
        addIncome(payment)
        this.actualMoney += goal.getSaving()
        this.goalList.removeAt(i)
        //TODO remove to the database
    }

    fun claimPrice(description: String, money: Float) {
        val payment = CashFlow(description, money, CashFlowType.RECOMPENSA, LocalDate.now())
        addIncome(payment)
        this.actualMoney += money
    }

}