package com.alvdela.smartspend.domain

class Expense (
    private var description: String = "Gasto",
    private var amount: Float,
    private var type: ExpenseType
){
    fun getDescription(): String{
        return this.description
    }

    fun getAmount(): Float{
        return this.amount
    }

    fun getType():ExpenseType{
        return this.type
    }
}