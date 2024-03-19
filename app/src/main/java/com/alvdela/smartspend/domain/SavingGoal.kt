package com.alvdela.smartspend.domain

class SavingGoal(
    private var description: String,
    private var goal: Float,
    private var saving: Float = 0f
) {
    private var archived = false

    fun getDescription(): String{
        return this.description
    }

    fun getGoal(): Float {
        return this.goal
    }

    fun getSaving(): Float{
        return this.saving
    }

    fun saveMoney(money: Float){
        if (money < 0){
            throw IllegalArgumentException("Suma negativa")
        }
        this.saving = getSaving() + money
    }

}