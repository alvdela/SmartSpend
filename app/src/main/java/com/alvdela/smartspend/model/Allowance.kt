package com.alvdela.smartspend.model

import java.time.LocalDate


class Allowance (
    private var name: String,
    private var nextPayment: LocalDate,
    private var amount: Float,
    private var type: AllowanceType)
{

    private var expired = false

    /* --- Setter y getter --- */
    fun getName(): String{
        return this.name
    }

    fun setName(newName: String){
        this.name = newName
    }

    fun getNextPayment(): LocalDate{
        return this.nextPayment
    }

    fun setNextPayment(newDay: LocalDate){
        this.nextPayment = newDay
    }

    fun getAmount(): Float{
        return this.amount
    }

    fun setAmount(newAmount: Float){
        this.amount = newAmount
    }

    fun getType(): AllowanceType{
        return this.type
    }

    fun setType(newType: AllowanceType){
        this.type = newType
    }

    /* Fin de setter y getter */

    /**
     * Comprueba si la asignacion esta caducada o eliminada
     */
    fun allowanceExpired(): Boolean{
        return this.expired
    }

    /**
     * Metodo que comprueba si ya ha sido o es el día de pago
     */
    fun checkPaymentDay(): Boolean{
        var payday = false

        if (LocalDate.now() >= getNextPayment()){
            payday = true
        }

        return payday
    }

    /**
     * Metodo para obtener la propina y programar el proximo ingreso automáticamente
     */
    fun getPayment(): Float{

        when(this.type){
            AllowanceType.PUNTUAL -> this.expired = true
            AllowanceType.SEMANAL -> setNextPayment(getNextPayment().plusDays(7))
            AllowanceType.MENSUAL -> setNextPayment(getNextPayment().plusMonths(1))
            AllowanceType.TRIMESTRAL -> setNextPayment(getNextPayment().plusMonths(3))
            AllowanceType.SEMESTRAL -> setNextPayment(getNextPayment().plusMonths(6))
            AllowanceType.ANUAL -> setNextPayment(getNextPayment().plusYears(1))
        }

        return getAmount()
    }

}