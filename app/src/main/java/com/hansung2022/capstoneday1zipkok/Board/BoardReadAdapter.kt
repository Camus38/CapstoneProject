package com.hansung2022.capstoneday1zipkok.Board

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ItemBoardBinding
import java.util.*
import kotlin.collections.ArrayList

//나중에 이미지 추가를 위해 context 받아오기, 게시글 정보, 게시글 고유번호 리스트 형태로 받기
class BoardReadAdapter(
    val context: Context,
    private val dataSet: ArrayList<BoardReadModel>,
    private val boardList: ArrayList<String>,
    val myuid : String
) :
    RecyclerView.Adapter<BoardReadAdapter.BoardViewHolder>() {

    val db = Firebase.firestore


    inner class BoardViewHolder(val binding: ItemBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    //뷰바인딩 형태로 입력
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BoardViewHolder {
        // Create a new view, which defines the UI of the list item

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_board, viewGroup, false)

        return BoardViewHolder(ItemBoardBinding.bind(view))


    }



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(boardViewHolder: BoardViewHolder, position: Int) {
        val boardReadModel = dataSet[position]

        //Long타입 시간 데이터 변화
        val format = SimpleDateFormat("yyyy.MM/dd HH:mm")
        val date = Date(boardReadModel.createdAt)

        //각 아이템에 보여질 것들. 생성시간, 작성자 닉네임, 글 제목 순.
        boardViewHolder.binding.tvCreatedAt.text = format.format(date).toString()
        boardViewHolder.binding.tvBoardNickName.text = boardReadModel.nickname
        boardViewHolder.binding.tvTitle.text = boardReadModel.title

//내가 작성한 글 체크표시
//        if(myuid.equals(boardReadModel.uid)){
//            boardViewHolder.binding.ivCheckBoardMine.isVisible = true
//        }




        //이미지 개수
        db.collection("Board").document("${boardReadModel.createdAt}").get().addOnSuccessListener { document->
            var imageCount = document["imageCount"] as Long
            boardViewHolder.binding.tvImageCount.text = imageCount.toString()
        }

        //댓글 개수

        db.collection("Board").document("${boardReadModel.createdAt}").collection("Comment")
            .get().addOnSuccessListener { documents ->
                var count : Int =0
                for (document in documents) {
                    count++
                }
                boardViewHolder.binding.tvCommentCount.text=count.toString()
            }
            .addOnFailureListener { exception ->
            }

        //좋아요 개수
        db.collection("Board").document("${boardReadModel.createdAt}").collection("Like")
            .get().addOnSuccessListener { documents->
                var countLike : Int =0
                for (document in documents) {
                    countLike++
                }
                boardViewHolder.binding.tvLikeCount.text=countLike.toString()

            }



        //각 아이템 누르면 아까 받아온 boardkey를 이용해서 깡통 액티비티 열고 거기에 boardkey정보로 데이터 받아오기.
        boardViewHolder.binding.root.setOnClickListener {
            val intent = Intent(context, BoardInsideActivity::class.java)
            intent.putExtra("boardkey", boardList[position])
            ContextCompat.startActivity(context, intent, null)
        }

    }



    override fun getItemCount() = dataSet.size

    //이거 없으면 꼬인다. 리사이클러뷰 중복버그 막아줌
    override fun getItemViewType(position: Int): Int {
        return position
    }




}