package com.alvdela.smartspend.model

import java.io.Serializable

class Family(
    private var familyName: String,
    private var emailFamily: String
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

    fun getChildrenNames(): List<String> {
        val childList = mutableListOf("")
        for ((userName, member) in members) {
            if (member is Child) {
                childList.add(userName)
            }
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

    /* Funciones para las tareas */

    fun getTaskList(): MutableList<Task> {
        return this.taskList
    }

    fun getTask(indexTask: Int): Task {
        return this.taskList[indexTask]
    }

    fun addTask(task: Task) {
        this.taskList.add(task)
    }

    fun removeTask(indexTask: Int) {
        addTaskToHistoric(getTaskList()[indexTask])
        this.taskList.removeAt(indexTask)
    }

    private fun addTaskToHistoric(task: Task){
        this.oldTask.add(0,task)
        if (oldTask.size > MAX_HISTORIC){
            removeTaskFromHistoric(oldTask.size)
        }
    }
    private fun removeTaskFromHistoric(indexTask: Int) {
        this.oldTask.removeAt(indexTask)
    }

    /* --- Metodos para asegurar la contraseña --- */

    /* fun getPassword():String{
         return this.passwordFamily
     }

     fun setPassword(password: String) {
         val salt = generateSalt()
         val hashedPassword = hashPassword(password, salt)
         this.passwordFamily = hashedPassword
     }

     fun checkPassword(password: String): Boolean {
         val hashedPassword = hashPassword(password, passwordFamily.substring(0, 8))
         return hashedPassword == passwordFamily
     }

     private fun generateSalt(): String {
         val salt = ByteArray(8)
         SecureRandom().nextBytes(salt)
         return salt.toString(Charsets.UTF_8)
     }

     private fun hashPassword(password: String, salt: String): String {
         val messageDigest = MessageDigest.getInstance("SHA-256")
         val bytes = messageDigest.digest("$password$salt".toByteArray(Charsets.UTF_8))
         return bytes.joinToString("") { "%02x".format(it) }
     }*/
}