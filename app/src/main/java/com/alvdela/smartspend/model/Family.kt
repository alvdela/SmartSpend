package com.alvdela.smartspend.model

import java.io.Serializable

class Family(
    private var familyName: String,
    private var emailFamily: String,
) : Serializable {
    private var members: MutableMap<String, Member> = mutableMapOf()
    private val taskList: MutableList<Task> = mutableListOf()
    private var oldTask: MutableList<Task> = mutableListOf()

    companion object{
        const val MAX_MEMBERS = 12
        const val MAX_HISTORIC = 50
    }

    fun addMember(member: Member): String {
        if (this.members.size < MAX_MEMBERS) {
            this.members[member.getUser()] = member
            return "Usuario añadido correctamente"
        }
        return "Numero máximo de usuarios alcanzado (12 miembros)"
    }

    fun deleteMember(user: String) {
        this.members.remove(user)
    }

    fun getMember(user: String): Member? {
        return this.members[user]
    }

    fun getMembers(): MutableMap<String, Member> {
        return this.members
    }

    fun getName(): String {
        return this.familyName
    }

    fun setName(newName: String) {
        this.familyName = newName
    }

    fun getEmail(): String {
        return this.emailFamily
    }

    fun setEmail(newEmail: String) {
        this.emailFamily = newEmail
    }

    fun getChildren(): List<Child> {
        val childList = mutableListOf<Child>()
        for ((_, member) in members) {
            if (member is Child) {
                childList.add(member)
            }
        }
        return childList.toList()
    }

    fun getMemberById(id: String): Member? {
        for ((_, member) in members) {
            if (member.getId() == id) {
                return member
            }
        }
        return null
    }

    fun getChildrenNames(): List<String> {
        val childList = mutableListOf("")
        for ((userName, member) in members) {
            if (member is Child) {
                childList.add(userName)
            }
        }
        if (childList.size > 1){
            childList.removeAt(0)
        }
        return childList.toList()
    }

    fun checkName(name: String): Boolean {
        return this.members.containsKey(name)
    }

    fun checkChildrenPayments() {
        for ((userName, member) in members) {
            if (member is Child) {
                member.getPayment()
            }
        }
    }

    fun isParent(name: String):Boolean{
        return members[name] is Parent
    }

    fun getPosition(user: String): Int{
        var index = 0
        for((key,_) in this.members){
            if (key == user){
                return index
            }
            index++
        }
        return 0
    }

    /* Funciones para las tareas */

    fun getTaskList(): MutableList<Task> {
        return this.taskList
    }

    fun getTaskOfChild(child: String): MutableList<Task>{
        val newList = mutableListOf<Task>()
        for (task in this.taskList){
            if (task.getChildName() == child || task.getChildName() == ""){
                newList.add(task)
            }
        }
        return newList
    }

    fun getTask(indexTask: Int): Task {
        return this.taskList[indexTask]
    }

    fun addTask(task: Task) {
        this.taskList.add(task)
    }

    fun removeTask(indexTask: Int) {
        addTaskToHistory(getTaskList()[indexTask])
        this.taskList.removeAt(indexTask)
    }

    fun addTaskToHistory(task: Task){
        this.oldTask.add(0,task)
        if (oldTask.size > MAX_HISTORIC){
            removeTaskFromHistoric(oldTask.size)
        }
    }

    fun getTaskHistory(): MutableList<Task> {
        return this.oldTask
    }

    fun removeTaskFromHistoric(indexTask: Int) {
        this.oldTask.removeAt(indexTask)
    }

    fun clearHistory(){
        this.oldTask.clear()
    }
}