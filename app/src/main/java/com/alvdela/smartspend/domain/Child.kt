package com.alvdela.smartspend.domain

import java.time.LocalDate

class Child(user: String) : Member(user) {

    private var actualMoney: Float = 0F
    private var allowanceList: MutableList<Allowance> = mutableListOf()
    private var cashFlowList: MutableList<CashFlow> = mutableListOf()
    private var goalList: MutableList<SavingGoal> = mutableListOf()

    fun getActualMoney(): Float {
        return this.actualMoney
    }

    fun setActualMoney(money: Float){
        this.actualMoney = money
    }

    fun addAllowance(allowance: Allowance) {
        this.allowanceList.add(allowance)
    }

    fun getAllowances(): MutableList<Allowance> {
        return allowanceList
    }

    fun getPayment() {
        val iterator = allowanceList.iterator()
        while (iterator.hasNext()){
            val allowance = iterator.next()
            if (allowance.checkPaymentDay()) {
                val payment = CashFlow(allowance.getName(), allowance.getAmount(), CashFlowType.INGRESO, LocalDate.now())
                addExpense(payment)
                actualMoney += allowance.getPayment()
            }
            if (allowance.allowanceExpired()){
                iterator.remove()
            }
        }

    }

    fun addExpense(cashFlow: CashFlow) {
        if (cashFlow.amount > getActualMoney()) {
            //Do nothing
        } else {
            actualMoney -= cashFlow.amount
            this.cashFlowList.add(0,cashFlow)
        }
    }

    fun getCashFlow(): MutableList<CashFlow> {
        return cashFlowList
    }

    fun addGoal(goal: SavingGoal) {
        this.goalList.add(goal)
    }

    fun getGoals(): MutableList<SavingGoal> {
        return this.goalList
    }

    fun claimGoal(i: Int){
        val goal = getGoals()[i]
        if (goal.isArchived()){
            this.actualMoney += goal.getSaving()
            val payment = CashFlow(goal.getDescription(), goal.getSaving(), CashFlowType.INGRESO, LocalDate.now())
            addExpense(payment)
            this.goalList.removeAt(i)
        }
    }

}