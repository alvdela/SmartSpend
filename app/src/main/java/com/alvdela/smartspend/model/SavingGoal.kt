package com.alvdela.smartspend.model

class SavingGoal(
    private var description: String,
    private var goal: Float,
    private var type: GoalType
) {
    private var achived = false
    private var saving: Float = 0f

    fun isArchived(): Boolean{
        return achived
    }

    fun getDescription(): String{
        return this.description
    }

    fun getGoal(): Float {
        return this.goal
    }

    fun getSaving(): Float{
        return this.saving
    }

    fun getType(): GoalType{
        return this.type
    }

    fun saveMoney(money: Float): Float{
        var rest = 0F
        if ((getSaving() + money) > getGoal()){
            rest = (getSaving() + money) - getGoal()
            this.saving = getGoal()
        }
        this.saving = getSaving() + money
        checkAchieved()
        return rest
    }

    private fun checkAchieved(){
        if (getSaving() == getGoal()){
            achived = true
        }
    }

    fun getMoneyLeft(): Float{
        return getGoal() - getSaving()
    }

}