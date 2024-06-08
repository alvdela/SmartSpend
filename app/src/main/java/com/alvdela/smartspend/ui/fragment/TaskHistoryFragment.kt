package com.alvdela.smartspend.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.ui.activity.ProfilesActivity
import com.alvdela.smartspend.adapter.TaskHistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskHistoryFragment : Fragment() {

    private lateinit var historyTasks: MutableList<Task>
    private lateinit var tasks: MutableList<Task>

    private var dataChanged = false

    private var uid = "mock"

    private lateinit var taskHistoryAdapter: TaskHistoryAdapter

    private lateinit var dialog: Dialog

    companion object {
        var configProfileOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyTasks = ContextFamily.family!!.getTaskHistory()
        tasks = ContextFamily.family!!.getTaskList()

        if (!ContextFamily.isMock){
            uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btDeleteAllTask = view.findViewById<ImageView>(R.id.btDeleteAllTask)
        btDeleteAllTask.setOnClickListener {
            deleteHistory()
        }

        showTaskHistory(view)

        val toolbarProfile = view.findViewById<Toolbar>(R.id.toolbar_profile)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbarProfile)
        toolbarProfile.setNavigationOnClickListener {
            // Cerrar el Fragment
            configProfileOpen = false
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            if (dataChanged){
                backToProfiles()
            }
        }
    }

    private fun deleteHistory() {
        showPopUp(R.layout.pop_up_delete)

        val tvDelete = dialog.findViewById<TextView>(R.id.tvDelete)
        tvDelete.text = resources.getString(R.string.clean_task_history)

        val cancelDelete = dialog.findViewById<Button>(R.id.cancelDelete)
        cancelDelete.setOnClickListener {
            dialog.dismiss()
        }

        val confirmDelete = dialog.findViewById<Button>(R.id.confirmDelete)
        confirmDelete.setOnClickListener {
            for (task in historyTasks){
                if (!ContextFamily.isMock){
                    deleteTaskFromDatabase(task.getId(), Constants.HISTORIC)
                }
            }
            ContextFamily.family!!.clearHistory()
            taskHistoryAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }
    }

    private fun showTaskHistory(view: View){
        val rvHistoryTask = view.findViewById<RecyclerView>(R.id.rvHistoryTask)
        if (historyTasks.isNotEmpty()){
            taskHistoryAdapter = TaskHistoryAdapter(tasksHistory = historyTasks
            ) { selectedTask -> reOpenTask(selectedTask) }

            rvHistoryTask.layoutManager = LinearLayoutManager(requireContext())
            rvHistoryTask.adapter = taskHistoryAdapter
            rvHistoryTask.itemAnimator = DefaultItemAnimator()
        }else{
            rvHistoryTask.visibility = View.INVISIBLE
        }
    }

    private fun reOpenTask(selectedTask: Int) {
        val task = historyTasks[selectedTask]
        showPopUp(R.layout.pop_up_log_out)

        val textViewInfo = dialog.findViewById<TextView>(R.id.tvlogOut)
        textViewInfo.text = resources.getString(R.string.reopen_task)

        val cancelButton = dialog.findViewById<Button>(R.id.cancelButtonLogOut)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButtonLogOut)
        confirmButton.setOnClickListener {
            task.reOpenTask()
            if (ContextFamily.isMock){
                ContextFamily.family!!.removeTaskFromHistoric(selectedTask)
                ContextFamily.family!!.addTask(task)
                taskHistoryAdapter.notifyDataSetChanged()
            }else{
                deleteTaskFromDatabase(task.getId(), Constants.HISTORIC)
                addTaskToDatabase(task, selectedTask, Constants.TASKS)
            }
            dataChanged = true
            dialog.dismiss()
        }
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(requireContext())
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun backToProfiles() {
        val intent = Intent(requireContext(), ProfilesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    /* Metodos de la base de datos */

    private fun addTaskToDatabase(task: Task, selectedTask: Int, typeOfTask: String) {
        var limitDate = ""
        if (task.getLimitDate() != null) {
            limitDate = task.getLimitDate()!!.format(Constants.dateFormat)
        }
        var completedDate = ""
        if (typeOfTask == Constants.HISTORIC && task.getCompletedDate() != null) {
            completedDate = task.getCompletedDate()!!.format(Constants.dateFormat)
        }
        var childId = ""
        if (task.getChild() != null){
            childId = task.getChild()!!.getId()
        }
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(typeOfTask)
            .add(
                hashMapOf(
                    "description" to task.getDescription(),
                    "limitDate" to limitDate,
                    "mandatory" to task.isMandatory(),
                    "price" to task.getPrice().toString(),
                    "state" to TaskState.toString(task.getState()),
                    "completedDate" to completedDate,
                    "child" to childId,
                    "assigned" to task.isAssigned()
                )
            )
            .addOnSuccessListener { document ->
                task.setId(document.id)
                ContextFamily.family!!.removeTaskFromHistoric(selectedTask)
                ContextFamily.family!!.addTask(task)
                taskHistoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al reabrir la tarea", Toast.LENGTH_SHORT).show()
                println("Error al reabrir la tarea")
            }
    }

    private fun deleteTaskFromDatabase(id: String, typeOfTask: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(typeOfTask)
            .document(id)
            .delete()
            .addOnSuccessListener {
                println("Tarea eliminada correctamente")
            }
            .addOnFailureListener { e ->
                println("Error al eliminar la tarea: $e")
            }
    }
}