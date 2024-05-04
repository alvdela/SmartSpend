package com.alvdela.smartspend.model

enum class AllowanceType {
    PUNTUAL, SEMANAL, MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL;

    companion object {
        fun fromString(text: String): AllowanceType? {
            return when (text.lowercase()) {
                "puntual" -> PUNTUAL
                "semanal" -> SEMANAL
                "mensual" -> MENSUAL
                "trimestral" -> TRIMESTRAL
                "semestral" -> SEMESTRAL
                "anual" -> ANUAL
                else -> null
            }
        }

        fun toString(allowanceType: AllowanceType): String {
            return when (allowanceType) {
                PUNTUAL -> "Puntual"
                SEMANAL -> "Semanal"
                MENSUAL -> "Mensual"
                TRIMESTRAL -> "Trimestral"
                SEMESTRAL -> "Semestral"
                ANUAL -> "Anual"
            }
        }
    }
}

