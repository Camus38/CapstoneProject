package com.hansung2022.capstoneday1zipkok.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.Todo.CustomDialog
import com.hansung2022.capstoneday1zipkok.Todo.Todo
import com.hansung2022.capstoneday1zipkok.Todo.TodoAdapter
import com.hansung2022.capstoneday1zipkok.Todo.TodoViewModel
import com.hansung2022.capstoneday1zipkok.databinding.FragmentTodoBinding

class TodoFragment : Fragment() {
    private var binding: FragmentTodoBinding? = null
    val viewModel: TodoViewModel by viewModels()
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        auth = Firebase.auth


        binding!!.todoRecyclerView.layoutManager = LinearLayoutManager(context)
        binding!!.todoRecyclerView.adapter = TodoAdapter(emptyList(),
            onClickDeleteIcon = {
                viewModel.deleteTodo(it)
            },
            onClickItem = {
                viewModel.toggle(it)
            }
        )



        // 추가 버튼
        binding!!.fabAdd.setOnClickListener {
            val dialog = CustomDialog()
            dialog.setButtonClickListener(object : CustomDialog.OnButtonClickListener {
                override fun onButton1Clicked(msg: String) { //받아온 msg인자 출력하기
                    val mymsg = msg
                    if (mymsg.isNotEmpty()) {
                        viewModel.addTodo(Todo(mymsg))
                    }
                }

            })
            fragmentManager?.let { it1 -> dialog.show(it1, "CustomDialog") }
        }

        //관찰, UI업데이트
        viewModel.todoLiveData.observe(this, Observer {
            (binding!!.todoRecyclerView.adapter as TodoAdapter).setData(it)
        })

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }




}

