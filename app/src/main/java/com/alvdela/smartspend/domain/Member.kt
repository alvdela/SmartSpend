package com.alvdela.smartspend.domain

open class Member(
    protected var user: String,
    protected var password: String = ""
) {

    internal fun getUser(): String{
        return user
    }

    internal fun setUser(user:String){
        this.user = user
    }

    internal fun setPassword(user:String){
        this.password = password
    }

    internal fun checkPassword(passwordInput: String): Boolean{
        return passwordInput == password
    }

}