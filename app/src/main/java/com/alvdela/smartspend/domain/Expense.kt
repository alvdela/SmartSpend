package com.alvdela.smartspend.domain

data class Expense (
    private var description: String = "Gasto",
    private var amount: Float,
    private var type: ExpenseType
)