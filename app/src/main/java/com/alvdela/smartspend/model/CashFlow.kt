package com.alvdela.smartspend.model

import java.math.BigDecimal
import java.time.LocalDate

data class CashFlow (
    val description: String = "gasto",
    val amount: BigDecimal,
    val type: CashFlowType,
    val date: LocalDate
)