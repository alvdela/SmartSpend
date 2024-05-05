package com.alvdela.smartspend.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Task (
    private var description: String,
    private var limitDate: LocalDate? = null,
    private var mandatory: Boolean = false,
    private var price: Float = 0f,
    private var state: TaskState
){
    private lateinit var id: String
    private var completedDate: LocalDate? = null
    private var child: Child? = null

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

    fun setCompletedDate(date: LocalDate){
        this.completedDate = date
    }

    fun getCompletedDate(): LocalDate?{
        return this.completedDate
    }

    // Getter and Setter for state
    fun getState(): TaskState {
        return state
    }

    fun setState(state: TaskState) {
        this.state = state
    }

    fun setChild(child: Child){
        this.child = child
    }

    fun getChildName(): String{
        return if (this.child != null){
            this.child!!.getUser()
        }else{
            ""
        }
    }

    fun getId(): String{
        return id
    }

    fun setId(id:String){
        this.id = id
    }

    fun givePrice(){
        if (this.state == TaskState.COMPLETE){
            this.child!!.claimPrice(getDescription(),getPrice())
        }
    }

    fun getDaysLeft():Int{
        if (getLimitDate() != null){
            return ChronoUnit.DAYS.between(LocalDate.now(), getLimitDate()).toInt()
        }
        return -10000
    }
}