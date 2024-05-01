import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Task
import com.alvdela.smartspend.model.TaskState
import com.alvdela.smartspend.ui.adapter.TaskViewHolder

class TaskCompleteAdapter(
    private var tasks: MutableList<Task> = mutableListOf(),
    private val completeTask: (Int, Int) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    private val filteredTasks: MutableList<Task> = mutableListOf()

    init {
        filterTasks()
    }

    fun filterTasks() {
        filteredTasks.clear()
        for (task in tasks) {
            if (task.getState() == TaskState.COMPLETE) {
                filteredTasks.add(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.item_task, parent, false))
    }

    override fun getItemCount(): Int = filteredTasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTasks[position]
        holder.render(task, completeTask, tasks.indexOf(task), position)
    }

    fun notifyNewTask(){
        filterTasks()
        notifyItemInserted(filteredTasks.size)
    }
}
