package com.alvdela.smartspend.domain

import java.io.Serializable
import java.security.MessageDigest
import java.security.SecureRandom

class Family(
    private var familyName: String,
    private var emailFamily: String
): Serializable{
    private var passwordFamily: String = ""
    private var members: MutableMap<String, Member> = mutableMapOf()

    /**
     * Funcion para añadir miembros a la familia
     */
    fun addMember(user:String, member: Member){
        this.members[user] = member
    }

    /**
     * Funcion para eliminar un miembro de la familia
     */
    fun deleteMember(user: String){
        this.members.remove(user)
    }

    /**
     * Funcion que devuelve los datos de un miembro de la familia
     */
    fun getMember(user: String): Member? {
        return this.members[user]
    }

    /* --- Aquí comienzan getter y setters --- */
    fun getName():String{
        return this.familyName
    }

    fun setName(newName: String){
        this.familyName = newName
    }

    fun getEmail():String{
        return this.emailFamily
    }

    fun setEmail(newEmail: String){
        this.emailFamily = newEmail
    }

    /* --- Metodos para asegurar la contraseña --- */
    fun getPassword():String{
        return this.passwordFamily
    }

    fun setPassword(password: String) {
        val salt = generateSalt()
        val hashedPassword = hashPassword(password, salt)
        this.passwordFamily = hashedPassword
    }

    /**
     * Comprueba si la contraseña es correcta
     */
    fun checkPassword(password: String): Boolean {
        val hashedPassword = hashPassword(password, passwordFamily.substring(0, 8))
        return hashedPassword == passwordFamily
    }

    /**
     * Genera un 'salt' para encriptar la contraseña
     */
    private fun generateSalt(): String {
        val salt = ByteArray(8)
        SecureRandom().nextBytes(salt)
        return salt.toString(Charsets.UTF_8)
    }

    /**
     * Encripta la contraseña con el 'salt' generado
     */
    private fun hashPassword(password: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = messageDigest.digest("$password$salt".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}