package com.hansung2022.capstoneday1zipkok.Todo

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ItemTodoBinding

//데이터 클래스
data class Todo(val text: String, var done: Boolean = false)

//아답터
class TodoAdapter(
    private var dataSet: List<DocumentSnapshot>,
    val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit,
    val onClickItem: (todo: DocumentSnapshot) -> Unit
) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {


    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TodoViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_todo, viewGroup, false)

        return TodoViewHolder(ItemTodoBinding.bind(view))
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(todoViewHolder: TodoViewHolder, position: Int) {
        val todo = dataSet[position]

        todoViewHolder.binding.todo.text = todo.getString("text") ?: ""

        todoViewHolder.binding.ivDelete.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }


        if ((todo.getBoolean("done") ?: false) == true) {
            todoViewHolder.binding.todo.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTextColor(Color.parseColor("#A6A4A4"))
            }
        } else {
            todoViewHolder.binding.todo.apply {
                paintFlags = 0
                setTextColor(Color.parseColor("#000000"))
            }
        }

        todoViewHolder.binding.todo.setOnClickListener {
            onClickItem.invoke(todo)
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun setData(newData: List<DocumentSnapshot>) {
        dataSet = newData
        notifyDataSetChanged()
    }

}