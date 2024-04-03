package com.alvdela.smartspend.domain

import java.time.LocalDate

data class CashFlow (
    val description: String = "Gasto",
    val amount: Float,
    val type: CashFlowType,
    val date: LocalDate
)