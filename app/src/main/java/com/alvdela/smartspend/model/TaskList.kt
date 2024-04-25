package com.alvdela.smartspend.model

class TaskList() {

    private var tasks: MutableList<Task> = mutableListOf()
    private var oldTask: MutableList<Task> = mutableListOf()

    fun addTask(task: Task){
        this.tasks.add(task)
    }

    fun removeTask(indexTask: Int){
        this.oldTask.add(this.tasks[indexTask])
        this.tasks.removeAt(indexTask)
    }

    fun removeTaskFromHistoric(indexTask: Int){
        this.oldTask.removeAt(indexTask)
    }

    private fun getTasks(): MutableList<Task>{
        return this.tasks
    }

    fun getOldTasks(): MutableList<Task>{
        return this.oldTask
    }

    fun getTasksPendientes(): MutableList<Task>{
        val pendientes = mutableListOf<Task>()
        for (i in getTasks()){
            if (i.getState() == TaskState.OPEN){
                pendientes.add(i)
            }
        }
        return pendientes
    }

    fun getTasksCompletadas(): MutableList<Task>{
        val completadas = mutableListOf<Task>()
        for (i in getTasks()){
            if (i.getState() == TaskState.COMPLETE){
                completadas.add(i)
            }
        }
        return completadas
    }

    fun getTasksMandatory(): MutableList<Task>{
        val mandatory = mutableListOf<Task>()
        for (i in getTasks()){
            if (i.isMandatory()){
                mandatory.add(i)
            }
        }
        return mandatory
    }

    fun getTasksNoMandatory(): MutableList<Task>{
        val noMandatory = mutableListOf<Task>()
        for (i in getTasks()){
            if (!i.isMandatory()){
                noMandatory.add(i)
            }
        }
        return noMandatory
    }
}