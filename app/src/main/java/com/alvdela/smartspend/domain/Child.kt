package com.alvdela.smartspend.domain

class Child(user: String) : Member(user) {

    private var actualMoney: Float = 0F
    private var allowanceList: MutableList<Allowance> = mutableListOf()
    private var expenseList: MutableList<Expense> = mutableListOf()
    private var goalList: MutableList<SavingGoal> = mutableListOf()

    fun getActualMoney(): Float {
        return this.actualMoney
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
                val payment = Expense(allowance.getName(), allowance.getAmount(), ExpenseType.INGRESO)
                addExpense(payment)
                actualMoney += allowance.getPayment()
            }
            if (allowance.allowanceExpired()){
                iterator.remove()
            }
        }

    }

    fun addExpense(expense: Expense): Boolean {
        return if (expense.getAmount() > getActualMoney()) {
            false
        } else {
            actualMoney -= expense.getAmount()
            this.expenseList.add(expense)
            true
        }
    }

    fun getExpenses(): MutableList<Expense> {
        return expenseList
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
            val payment = Expense(goal.getDescription(), goal.getSaving(), ExpenseType.INGRESO)
            addExpense(payment)
            this.goalList.removeAt(i)
        }
    }

}