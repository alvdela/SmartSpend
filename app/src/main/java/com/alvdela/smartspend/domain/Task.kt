package com.alvdela.smartspend.domain

import java.time.LocalDate

class Task (
    private var description: String,
    private var limitDate: LocalDate?,
    private var mandatory: Boolean = false,
    private var price: Int = 0,
    private var state: TaskState
){
    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getLimitDate(): LocalDate? {
        return limitDate
    }

    fun setLimitDate(limitDate: LocalDate?) {
        this.limitDate = limitDate
    }

    fun isMandatory(): Boolean {
        return mandatory
    }

    fun setMandatory(mandatory: Boolean) {
        this.mandatory = mandatory
    }

    fun getPrice(): Int {
        return price
    }

    fun setPrice(price: Int) {
        this.price = price
    }

    // Getter and Setter for state
    fun getState(): TaskState {
        return state
    }

    fun setState(state: TaskState) {
        this.state = state
    }

}