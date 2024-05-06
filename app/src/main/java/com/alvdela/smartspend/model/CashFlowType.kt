package com.alvdela.smartspend.model

import java.util.Locale

enum class CashFlowType {
    COMIDA, OCIO, COMPRAS, OTROS, INGRESO, RECOMPENSA;

    companion object {
        fun fromString(text: String): CashFlowType? {
            return when (text.lowercase()) {
                "comida" -> COMIDA
                "ocio" -> OCIO
                "compras" -> COMPRAS
                "otros" -> OTROS
                "ingreso" -> INGRESO
                "recompensa" -> RECOMPENSA
                else -> null
            }
        }

        fun toString(cashFlowType: CashFlowType): String {
            return when (cashFlowType) {
                COMIDA -> "Comida"
                OCIO -> "Ocio"
                COMPRAS -> "Compras"
                OTROS -> "Otros"
                INGRESO -> "Ingreso"
                RECOMPENSA -> "Recompensa"
            }
        }
    }
}