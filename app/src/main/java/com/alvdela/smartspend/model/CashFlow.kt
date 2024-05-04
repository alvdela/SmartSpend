package com.alvdela.smartspend.model

import java.time.LocalDate

data class CashFlow (
    val description: String = "gasto",
    val amount: Float,
    val type: CashFlowType,
    val date: LocalDate
)