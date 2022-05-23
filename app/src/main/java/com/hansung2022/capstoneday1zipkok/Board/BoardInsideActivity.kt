package com.hansung2022.capstoneday1zipkok.Board

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hansung2022.capstoneday1zipkok.BoardImage.GalleryReadActivity
import com.hansung2022.capstoneday1zipkok.BoardImage.MultiImageReadAdapter
import com.hansung2022.capstoneday1zipkok.Comment.CommentAdapter
import com.hansung2022.capstoneday1zipkok.Comment.CommentModel
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ActivityBoardInsideBinding
import java.lang.NullPointerException

data class Like(
    val uid: String = "",
)

class BoardInsideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardInsideBinding
    private lateinit var auth: FirebaseAuth

    //이미지 받기
    var imageList: MutableList<String> = mutableListOf()
    var imageListToEdit: ArrayList<Uri> = arrayListOf()
    var imageListString: MutableList<String> = mutableListOf()
    var imageListStringToEdit: ArrayList<String> = arrayListOf()
    private lateinit var multiImageReadAdapter: MultiImageReadAdapter


    //댓글
    var commentData: MutableList<CommentModel> = mutableListOf()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var boardKey: String

    val db = Firebase.firestore


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBoardInsideBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        //uid받기
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        boardKey = intent.getStringExtra("boardkey").toString()

        //이미지, 데이터 받기 전에 수정 들어가지면 데이터 누락되기 때문에
        binding.tvBoardUpdate.isEnabled = false


        //이미지 받기

        multiImageReadAdapter = MultiImageReadAdapter(imageList, this)
        binding.rvImageGet.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvImageGet.adapter = multiImageReadAdapter
        //updateImage()



        binding.btImageOpen.setOnClickListener {
            if(binding.btImageOpen.text.toString().equals("이미지 펼치기")){
                multiImageReadAdapter.notifyDataSetChanged()
                binding.rvImageGet.isVisible = true
                binding.btImageOpen.text = "이미지 접기"
            }else{
                binding.rvImageGet.setVisibility(View.GONE)
                binding.btImageOpen.text = "이미지 펼치기"
            }
        }



        //게시판 진입시 이미 Like한 상태면 색칠, 아니면 디폴트
        db.collection("Board").document(boardKey).collection("Like").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val likeUid = document["uid"].toString()
                    if (likeUid == uid) {
                        binding.ivLike.setImageResource(R.drawable.ic_baseline_thumb_up_24_color)
                        //binding.ivLike.isEnabled = false
                    }
                }
            }

        binding.ivLike.setOnClickListener {
            //좋아요 db에 작성할 때
            val myuploadUid = Like(uid)


            //좋아요 했는지 db에서 서칭 -> Uid가 있으면 삭제, Uid가 없으면 등록
            db.collection("Board").document(boardKey).collection("Like").get()
                .addOnSuccessListener { documents ->

                    var existLikeUid: Boolean = false

                    for (document in documents) {
                        val likeUid = document["uid"].toString()
                        //이미 좋아요를 눌렀으면
                        if (likeUid == uid) {
                            existLikeUid = true
                            break
                        }
                        //안 눌렸으면
                        else {
                            existLikeUid = false
                        }
                    }
                    //좋아요가 되어 있는 상태 -> 삭제, 무색으로 변경
                    if (existLikeUid == true) {
                        db.collection("Board").document(boardKey).collection("Like").document(uid)
                            .delete()
                        binding.ivLike.setImageResource(R.drawable.ic_baseline_thumb_up_24)
                        Toast.makeText(this, "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        db.collection("Board").document(boardKey).collection("Like").document(uid)
                            .set(myuploadUid)
                        binding.ivLike.setImageResource(R.drawable.ic_baseline_thumb_up_24_color)
                        Toast.makeText(this, "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                    }
                }


        }


        multiImageReadAdapter.notifyDataSetChanged()

        //이미지 눌러서 확대
        multiImageReadAdapter.imageClick = object : MultiImageReadAdapter.ImageClick {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@BoardInsideActivity, GalleryReadActivity::class.java)
                intent.putExtra("imageUri", imageList[position])
                startActivity(intent)

            }
        }


        //글 수정 삭제를 위해 게시글 작성자 uid 찾기
        var writerUid: String = ""


        //boardkey를 바탕으로 댓글 구성//////////////////
        var nickname: String = ""

        readCommentData(boardKey)
        commentAdapter = CommentAdapter(this, commentData, uid, boardKey)
        binding.rvComment.adapter = commentAdapter
        binding.rvComment.layoutManager = LinearLayoutManager(this)


        db.collection("UserInfo").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    nickname = document["nickname"].toString()
                } else {
                }
            }
            .addOnFailureListener { exception ->
            }


        //댓글 입력버튼
        binding.ivCommentInput.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("댓글 입력")
                .setMessage("댓글을 입력하시겠습니까?")
                .setNegativeButton("댓글 입력") { _, _ ->
                    var commentMsg = binding.etComment.text.toString()

                    val time = System.currentTimeMillis()
                    val commentModel = CommentModel(uid, nickname, commentMsg, time)
                    db.collection("Board").document(boardKey).collection("Comment")
                        .document("$time").set(commentModel)
                    commentData.clear()
                    readCommentData(boardKey)
                    binding.etComment.setText("")
                    Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("취소") { _, _ -> }
                .create()
                .show()


        }

        //////commentAdapter에서 빼온 인터페이스 ->댓글 삭제 처리
        commentAdapter.itemClick = object : CommentAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                AlertDialog.Builder(this@BoardInsideActivity)
                    .setTitle("댓글 삭제")
                    .setMessage("댓글을 삭제하시겠습니까?")
                    .setNegativeButton("댓글 삭제") { _, _ ->
                        db.collection("Board").document(boardKey).collection("Comment")
                            .document(commentData[position].createdAt.toString()).delete()
                        commentData.clear()
                        binding.rvComment.isVisible = false
                        binding.rvComment.isVisible = true
                        readCommentData(boardKey)
                        Toast.makeText(baseContext, "댓글 삭제 완료", Toast.LENGTH_SHORT).show()
                    }
                    .setPositiveButton("취소") { _, _ ->
                    }
                    .create()
                    .show()

            }
        }


        //boardkey를 바탕으로 게시글 재구성
        db.collection("Board").document(boardKey)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val boardTitle = document["title"].toString()
                    val boardMsg = document["msg"].toString()
                    val boardWriter = document["nickname"].toString()
                    val imageCount = document["imageCount"].toString().toInt()
                    writerUid = document["uid"].toString()
                    imageListString = document["imageNameList"] as MutableList<String>

                    updateImage()
                    Log.d("uid", writerUid.toString())
                    // Thread.sleep(1000)
                    Log.d("imageList!!", imageList.toString())

                    if (imageCount != 0) {
                        binding.btImageOpen.setVisibility(View.VISIBLE)
                        //binding.btImageClose.setVisibility(View.VISIBLE)
                    }

                    //게시글 작성자가 본인인지 확인 -> 수정, 삭제 버튼 나타내
                    if (writerUid.equals(uid)) {
                        binding.tvBoardDelete.isVisible = true
                        binding.tvBoardUpdate.isVisible = true
                    }


                    binding.tvBoardInsideTitle.text = boardTitle
                    binding.tvBoardInsideMsg.text = boardMsg
                    binding.tvBoardInsideWriter.text = boardWriter
                }
            }
            .addOnFailureListener { exception ->

            }

        //게시글 수정
        binding.tvBoardUpdate.setOnClickListener {
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putStringArrayListExtra("imageListStringToEdit", imageListStringToEdit)
            intent.putParcelableArrayListExtra("imageListToEdit", imageListToEdit)
            startActivity(intent)
            finish()
        }

        //게시글 삭제
        binding.tvBoardDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("삭제 안내")
                .setMessage("게시글을 삭제하시겠습니까?")
                .setPositiveButton("취소") { _, _ -> }
                .setNegativeButton("삭제") { _, _ ->
                    db.collection("Board").document(boardKey).delete()
                    Toast.makeText(this, "게시글이 삭제 되었습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
                .create()
                .show()


        }


    }

    private fun readCommentData(boardkey: String) {

        db.collection("Board").document(boardkey).collection("Comment")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {


                    //얘도 초기화
                    commentData.add(CommentModel(
                        document["uid"] as String,
                        document["nickname"] as String,
                        document["msg"] as String,
                        document["createdAt"] as Long
                    ))
                    commentAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun updateImage() {
        val storage = Firebase.storage


        if (imageListString.size == 0) {
            binding.pbBoardInside.isVisible = false
            binding.tvBoardUpdate.isEnabled = true

        } else {
            for (image in imageListString) {
                try {
                    val storageRef = storage.reference.child("BoardImage").child(boardKey)
                        .child("$image.image").downloadUrl.addOnSuccessListener {
                            imageList.add(it.toString())
                            imageListToEdit.add(it)
                            imageListStringToEdit.add(image)

                            //사진 전부 다운로드 받아야 로딩 사라지게
                            if (imageList.size == imageListString.size) {
                                binding.pbBoardInside.isVisible = false
                                binding.tvBoardUpdate.isEnabled = true
                            }


                        }.addOnFailureListener {
                            // Handle any errors
                        }

                } catch (e: NullPointerException) {
                }

            }


        }






        multiImageReadAdapter.notifyDataSetChanged()
    }
}

