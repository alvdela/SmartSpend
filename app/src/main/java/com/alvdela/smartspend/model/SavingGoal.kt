package com.alvdela.smartspend.model

import java.math.BigDecimal

class SavingGoal(
    private var description: String,
    private var goal: BigDecimal,
    private var type: GoalType
) {
    private lateinit var id: String
    private var achived = false
    private var saving: BigDecimal = BigDecimal("0")

    fun isArchived(): Boolean{
        return achived
    }

    fun getDescription(): String{
        return this.description
    }

    fun getGoal(): BigDecimal {
        return this.goal
    }

    fun getSaving(): BigDecimal{
        return this.saving
    }

    fun getType(): GoalType{
        return this.type
    }

    fun getId(): String{
        return id
    }

    fun setId(id:String){
        this.id = id
    }

    fun saveMoney(money: BigDecimal): BigDecimal{
        var rest = BigDecimal(0)
        if ((getSaving() + money) > getGoal()){
            rest = (getSaving() + money) - getGoal()
            this.saving = getGoal()
        }
        this.saving = getSaving() + money
        checkAchieved()
        return rest
    }

    private fun checkAchieved(){
        if (getSaving().compareTo(getGoal()) == 0){
            achived = true
        }
    }

    fun getMoneyLeft(): BigDecimal{
        return getGoal() - getSaving()
    }

}