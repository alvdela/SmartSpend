package com.alvdela.smartspend.model

import java.security.MessageDigest

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

    internal fun getPassword(): String{
        return this.password
    }

    internal fun setPassword(password:String){
        if (password.length >= 4) this.password = hashPassword(password)
        else this.password = ""
    }

    internal fun checkPassword(passwordInput: String): Boolean{
        if (this.password.length >= 4) return hashPassword(passwordInput) == this.password
        else return true
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}