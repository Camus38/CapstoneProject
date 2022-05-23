package com.hansung2022.capstoneday1zipkok.Comment

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ItemCommentBinding
import java.util.*

class CommentAdapter(
    private val context: Context,
    private val dataSet: MutableList<CommentModel>,
    private val myUid: String,
    private val boardKey: String,
) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    val db = Firebase.firestore

    //댓글 작성자가 게시글 작성자인지 확인하기 위한 용도
    var boardWriterUid: String = ""
    var commentWriterUid : String = ""

    //삭제 클릭 이벤트
    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: CommentAdapter.ItemClick? = null


    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_comment, viewGroup, false)

        return CommentViewHolder(ItemCommentBinding.bind(view))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val commentModel = dataSet[position]

        val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
        val date = Date(commentModel.createdAt)

        holder.binding.commentTime.text = format.format(date).toString()
        holder.binding.commentNickname.text = commentModel.nickname
        holder.binding.commentMsg.text = commentModel.msg




        //클릭 이벤트(삭제)
        if (itemClick != null) {
            holder.binding.tvCommentDelete.setOnClickListener {
                itemClick?.onClick(it, position)
            }
        }

        //게시물 작성자와 댓글작성자가 동일 할 경우 닉네임에 색 지정

        db.collection("Board").document(boardKey)
            .get()
            .addOnSuccessListener { result ->

                boardWriterUid = result["uid"] as String
                if (boardWriterUid.equals(commentModel.uid)) {
                    holder.binding.commentNickname.setTextColor(Color.parseColor("#dd9494"))

                }
            }

            .addOnFailureListener { exception ->

            }

        if (myUid.equals(commentModel.uid)) {
            holder.binding.ivCheckCommentMine.isVisible = true
            holder.binding.tvCommentDelete.isVisible = true
        }


    }

    override fun getItemCount() = dataSet.size



    override fun getItemViewType(position: Int): Int {
        return position
    }





}