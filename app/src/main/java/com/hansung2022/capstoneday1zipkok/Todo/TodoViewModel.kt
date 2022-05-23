package com.hansung2022.capstoneday1zipkok.Todo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

var currentTime : Long = System.currentTimeMillis()
class TodoViewModel : ViewModel() {

    val db = Firebase.firestore
    val todoLiveData = MutableLiveData<List<DocumentSnapshot>>()
    var addCount : Int = 0


    init {
        fetchData()
    }

    fun fetchData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
//            db.collection(user.uid)
            db.collection("TodoList").document("Todolist").collection(user.uid)
                .addSnapshotListener { value1, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    if (value1 != null) {
//                        todoLiveData.value = value1.documents
                        todoLiveData.value = value1.documents
                    }
                }
        }
    }

    fun addTodo(todo: Todo) {
        val user = FirebaseAuth.getInstance().currentUser?.let { user->
            db.collection("TodoList").document("Todolist").collection(user.uid).document("${currentTime}${addCount}${todo.text}").set(todo)
//            db.collection(user.uid).document("${currentTime}${addCount}${todo.text}").set(todo)
            addCount++

        }
    }

    fun deleteTodo(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let {
//            db.collection(it.uid).document(todo.id).delete()
            db.collection("TodoList").document("Todolist").collection(it.uid).document(todo.id).delete()
        }
    }

    fun toggle(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let {
            val done = todo.getBoolean("done") ?: false
//            db.collection(it.uid).document(todo.id).update("done", !done)
            db.collection("TodoList").document("Todolist").collection(it.uid).document(todo.id).update("done", !done)
        }
    }
}