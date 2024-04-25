package com.alvdela.smartspend.model

import java.time.LocalDate

class Task (
    private var description: String,
    private var limitDate: LocalDate?,
    private var mandatory: Boolean = false,
    private var price: Float = 0f,
    private var state: TaskState
){
    private var expired = false
    private lateinit var child: Child

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

    fun getPrice(): Float {
        return price
    }

    fun setPrice(price: Float) {
        this.price = price
    }

    // Getter and Setter for state
    fun getState(): TaskState {
        return state
    }

    fun setState(state: TaskState) {
        this.state = state
    }

    fun isExpired(): Boolean {
        return expired
    }

    fun setChild(child: Child){
        this.child = child
    }

    fun givePrice(){
        if (this.state == TaskState.COMPLETE){
            this.child.claimPrice(getDescription(),getPrice())
            setState(TaskState.DELETED)
        }
    }
}