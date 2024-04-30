package com.alvdela.smartspend.model

class SavingGoal(
    private var description: String,
    private var goal: Float,
    private var saving: Float = 0f,
    private var type: GoalType
) {
    private var archived = false

    fun isArchived(): Boolean{
        return archived
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
        return rest
    }

    fun checkArchieved(){
        if (getSaving() == getGoal()){
            archived = true
        }
    }

}