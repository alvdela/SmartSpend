/*
package com.alvdela.smartspend.firebase

import android.util.Log
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.model.Allowance
import com.alvdela.smartspend.model.AllowanceType
import com.alvdela.smartspend.model.CashFlow
import com.alvdela.smartspend.model.CashFlowType
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.model.GoalType
import com.alvdela.smartspend.model.Member
import com.alvdela.smartspend.model.MemberType
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.model.SavingGoal
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseManager private constructor() {
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private lateinit var collectionRef: CollectionReference

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }

    */
/**
     * Funcion creada para comprobar la conexión con Firebase
     *//*

    fun test() {
        collectionRef = firebaseFirestore.collection("test")
        collectionRef.document(FAMILY).set(
            hashMapOf(
                "familyName" to "test",
                "familyEmail" to "email@email.com"
            )
        )
    }

    fun getFamily(collection: String) {
        collectionRef = firebaseFirestore.collection(collection)

        collectionRef
            .document(FAMILY)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val familyName = document.getString("familyName")
                    val familyEmail = document.getString("familyEmail")
                    val family = Family(familyName!!, familyEmail!!)
                    ContextFamily.family = family
                }
            }
            .addOnFailureListener { exception ->
                println(exception)
            }
    }

    fun updateFamily(family: Family) {
        collectionRef.document(FAMILY).update(
            mapOf(
                "familyName" to family.getName(),
                "familyEmail" to family.getEmail()
            )
        )
    }

    fun getMembers(family: Family) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (MemberType.fromString(document.getString("type")!!) == MemberType.PARENT) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val parent = Parent(user, password)
                        family.addMember(parent)
                    } else if (MemberType.fromString(document.getString("type")!!) == MemberType.CHILD) {
                        val user = document.getString("user")!!
                        val password = document.getString("password")!!
                        val money = document.getLong("money")!!.toFloat()
                        val child = Child(user, password)
                        child.setActualMoney(money)
                        family.addMember(child)
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting document: $exception")
            }
    }

    fun addParent(parent: Parent) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(parent.getUser())
            .set(
                hashMapOf(
                    "user" to parent.getUser(),
                    "password" to parent.getPassword(),
                    "type" to MemberType.toString(MemberType.PARENT)
                )
            )
    }

    fun addChild(child: Child) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getUser())
            .set(
                hashMapOf(
                    "user" to child.getUser(),
                    "password" to child.getPassword(),
                    "money" to child.getActualMoney(),
                    "type" to MemberType.toString(MemberType.CHILD)
                )
            )
    }

    fun updateParent(parent: Parent) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(parent.getUser())
            .update(
                mapOf(
                    "user" to parent.getUser(),
                    "password" to parent.getPassword(),
                    "type" to MemberType.toString(MemberType.PARENT)
                )
            )
            .addOnSuccessListener {
                println("Datos del padre actualizados correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar datos del padre: $e")
            }
    }

    fun updateChild(child: Child) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getUser())
            .update(
                mapOf(
                    "user" to child.getUser(),
                    "password" to child.getPassword(),
                    "money" to child.getActualMoney(),
                    "type" to MemberType.toString(MemberType.CHILD)
                )
            )
            .addOnSuccessListener {
                println("Datos del niño actualizados correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar datos del niño: $e")
            }
    }

    fun deleteMember(member: String) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(member)
            .delete()
            .addOnSuccessListener {
                println("Documento eliminado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar documento: $e")
            }
    }

    fun getTask(family: Family, typeOfTask: String) {
        collectionRef
            .document(FAMILY)
            .collection(typeOfTask)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val completedDateString = document.getString("completedDate")!!
                    val description = document.getString("description")!!
                    val limitDateString = document.getString("limitDate")!!
                    var limitDate: LocalDate? = null
                    if (limitDateString != "") {
                        limitDate = LocalDate.parse(limitDateString, dateFormat)
                    }
                    val mandatory = document.getBoolean("mandatory")!!
                    val price = document.getDouble("price")!!.toFloat()
                    var state = TaskState.OPEN
                    when (document.getString("state")) {
                        "OPEN" -> state = TaskState.OPEN
                        "COMPLETE" -> state = TaskState.COMPLETE
                    }
                    val task = Task(description, limitDate, mandatory, price, state)
                    val id = document.id
                    task.setId(id)
                    if (state == TaskState.COMPLETE) {
                        val child = family.getMember(document.getString("child")!!) as Child
                        task.setChild(child)
                    }
                    if (typeOfTask == HISTORIC) {
                        var completedDate: LocalDate?
                        if (completedDateString != "") {
                            completedDate = LocalDate.parse(completedDateString, dateFormat)
                            task.setCompletedDate(completedDate)
                        }
                    }
                    family.addTask(task)
                }
            }
    }

    fun addTask(task: Task, typeOfTask: String) {
        var limitDate = ""
        if (task.getLimitDate() != null) {
            limitDate = task.getLimitDate()!!.format(dateFormat)
        }
        var completedDate = ""
        if (typeOfTask == HISTORIC && task.getCompletedDate() != null) {
            completedDate = task.getCompletedDate()!!.format(dateFormat)
        }
        collectionRef
            .document(FAMILY)
            .collection(typeOfTask)
            .add(
                hashMapOf(
                    "description" to task.getDescription(),
                    "limitDate" to task.getLimitDate(),
                    "mandatory" to task.isMandatory(),
                    "price" to task.getPrice(),
                    "state" to TaskState.toString(task.getState()),
                    "completedDate" to completedDate,
                    "child" to task.getChildName()
                )
            )
            .addOnSuccessListener { document ->
                task.setId(document.id)
            }
            .addOnFailureListener { e ->
                println("Error al agregar documento: $e")
            }
    }

    fun deleteTask(id: String, typeOfTask: String) {
        collectionRef.document(FAMILY).collection(typeOfTask).document(id).delete()
            .addOnSuccessListener {
                println("Documento eliminado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar documento: $e")
            }
    }

    fun getAllowances(child: Child) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child.getUser())
            .collection(ALLOWANCES)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name")!!
                    val nextPaymentString = document.getString("limitDate")!!
                    val nextPayment = LocalDate.parse(nextPaymentString, dateFormat)
                    val amount = document.getDouble("amount")!!.toFloat()
                    var type = AllowanceType.fromString(document.getString("type")!!)
                    if (type == null) {
                        type = AllowanceType.PUNTUAL
                    }
                    val id = document.id
                    val allowance = Allowance(name, nextPayment, amount, type)
                    allowance.setId(id)
                    child.addAllowance(allowance)
                }
            }
    }

    fun addAllowance(child: String, allowance: Allowance) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(ALLOWANCES)
            .add(
                hashMapOf(
                    "name" to allowance.getName(),
                    "nextPayment" to allowance.getNextPayment().format(dateFormat),
                    "amount" to allowance.getAmount(),
                    "type" to AllowanceType.toString(allowance.getType()),
                    "expired" to allowance.allowanceExpired()
                )
            )
            .addOnSuccessListener { document ->
                allowance.setId(document.id)
            }
            .addOnFailureListener { e ->
                println("Error al agregar documento: $e")
            }
    }

    fun updateAllowance(child: String, allowance: Allowance) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(ALLOWANCES)
            .document(allowance.getId())
            .update(
                mapOf(
                    "name" to allowance.getName(),
                    "nextPayment" to allowance.getNextPayment().format(dateFormat),
                    "amount" to allowance.getAmount(),
                    "type" to AllowanceType.toString(allowance.getType()),
                    "expired" to allowance.allowanceExpired()
                )
            )
            .addOnSuccessListener {
                println("Asignación actualizada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar asignación: $e")
            }
    }

    fun deleteAllowance(child: String, allowance: Allowance) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(ALLOWANCES)
            .document(allowance.getId())
            .delete()
            .addOnSuccessListener {
                println("Documento eliminado correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar documento: $e")
            }
    }

    fun getCashFlow(child: String): MutableList<CashFlow> {
        val cashFlowList = mutableListOf<CashFlow>()
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(CASHFLOW)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val amount = document.getDouble("amount")!!.toFloat()
                    val type = CashFlowType.fromString(document.getString("type")!!)
                    val date = LocalDate.parse(document.getString("date")!!, dateFormat)
                    val cashFlow = CashFlow(description, amount, type!!, date)
                    var index = 0
                    while (index < cashFlowList.size && cashFlowList[index].date.isAfter(cashFlow.date)) {
                        index++
                    }
                    cashFlowList.add(index, cashFlow)
                }
            }
        return cashFlowList
    }

    fun addCashFlow(child: String, cashFlow: CashFlow) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(CASHFLOW)
            .add(
                hashMapOf(
                    "description" to cashFlow.description,
                    "amount" to cashFlow.amount,
                    "type" to CashFlowType.toString(cashFlow.type),
                    "date" to cashFlow.date.format(dateFormat)
                )
            )
            .addOnSuccessListener {
                println("Documento añadido correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al añadir documento: $e")
            }
    }

    fun getGoals(child: String): MutableList<SavingGoal> {
        val goals = mutableListOf<SavingGoal>()
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description")!!
                    val savingGoal = document.getDouble("goal")!!.toFloat()
                    val saving = document.getDouble("saving")!!.toFloat()
                    val type = GoalType.fromString(document.getString("type")!!)
                    val id = document.id

                    val goal = SavingGoal(description, savingGoal, type!!)
                    goal.saveMoney(saving)
                    goal.setId(id)

                    goals.add(goal)
                }
            }
        return goals
    }

    fun addGoal(child: String, goal: SavingGoal) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .add(
                hashMapOf(
                    "description" to goal.getDescription(),
                    "goal" to goal.getGoal(),
                    "type" to GoalType.toString(goal.getType()),
                    "saving" to goal.getSaving(),
                    "archived" to goal.isArchived()
                )
            )
            .addOnSuccessListener { document ->
                goal.setId(document.id)
            }
            .addOnFailureListener { e ->
                println("Error al agregar documento: $e")
            }
    }

    fun updateGoal(child: String, goal: SavingGoal) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .document(goal.getId()) // Suponiendo que tienes un ID para la meta
            .update(
                mapOf(
                    "description" to goal.getDescription(),
                    "goal" to goal.getGoal(),
                    "type" to GoalType.toString(goal.getType()),
                    "saving" to goal.getSaving(),
                    "archived" to goal.isArchived()
                )
            )
            .addOnSuccessListener {
                println("Meta actualizada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar meta: $e")
            }
    }

    fun deleteGoal(child: String, goalId: String) {
        collectionRef
            .document(FAMILY)
            .collection(MEMBERS)
            .document(child)
            .collection(GOALS)
            .document(goalId)
            .delete()
            .addOnSuccessListener {
                println("Meta eliminada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar meta: $e")
            }
    }

}
*/
