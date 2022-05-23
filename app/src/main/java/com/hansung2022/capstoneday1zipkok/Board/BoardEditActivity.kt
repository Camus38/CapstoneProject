package com.hansung2022.capstoneday1zipkok.Board

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hansung2022.capstoneday1zipkok.BoardImage.GalleryActivity
import com.hansung2022.capstoneday1zipkok.BoardImage.MultiImageAdapter
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ActivityBoardEditBinding
import java.lang.Exception
import kotlin.concurrent.thread

class BoardEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardEditBinding
    private lateinit var auth: FirebaseAuth
    var imageList: MutableList<Uri> = mutableListOf()
    var imageListCheck: MutableList<Uri> = mutableListOf()
    var deleteImageList: MutableList<Uri> = mutableListOf()
    var addImageList: MutableList<Uri> = mutableListOf()
    var imageListString: MutableList<String> = mutableListOf()
    val multiImageAdapter = MultiImageAdapter(imageList, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBoardEditBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        val db = Firebase.firestore

        var getImage_btn = findViewById<ImageView>(R.id.iv_getImage2)
        // 리사이클러뷰
        var recyclerview = findViewById<RecyclerView>(R.id.rv_imageAdd2)

        //boardkey값 넘겨받기
        val boardKey = intent.getStringExtra("boardKey").toString()

        //사진 이름 boardinsideActivty로 부터 넘겨 받기
        var imageListStringFromRead = intent.getStringArrayListExtra("imageListStringToEdit")


        //사진 주소 boardinsideActivty로 부터 넘겨 받고 imageList에 저장
        val imageListFromBoard = intent.getParcelableArrayListExtra<Uri>("imageListToEdit")
        if (imageListFromBoard != null) {
            for (item in imageListFromBoard) {
                imageList.add(item)
            }
            binding.tvImageCount2.text = imageList.size.toString()
            multiImageAdapter.notifyDataSetChanged()
        }


        //이미지 추가
        getImage_btn.setOnClickListener {

            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.action = Intent.ACTION_PICK

            startActivityForResult(intent, 200)
        }

        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerview.layoutManager = layoutManager
        recyclerview.adapter = multiImageAdapter
        multiImageAdapter.notifyDataSetChanged()

        //x 눌러서 이미지 삭제
        multiImageAdapter.itemClick = object : MultiImageAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {

                try {
                    deleteImageList.add(imageList[position])
                    addImageList.remove(imageList[position])
                    imageListStringFromRead?.removeAt(position)
                    imageList.removeAt(position)

                } catch (e: IndexOutOfBoundsException) {
                }


                multiImageAdapter.notifyDataSetChanged()
                binding.tvImageCount2.text = imageList.size.toString()
            }
        }
        //이미지 눌러서 확대
        multiImageAdapter.imageClick = object : MultiImageAdapter.ImageClick {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@BoardEditActivity, GalleryActivity::class.java)
                intent.putExtra("imageUri", imageList[position])
                startActivity(intent)
            }
        }

        //기존 데이터 읽어오기(제목, 내용)
        db.collection("Board").document(boardKey)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val boardTitle = document["title"].toString()
                    val boardMsg = document["msg"].toString()

                    binding.tvBoardEditTitle.setText(boardTitle)
                    binding.tvBoardEditMsg.setText(boardMsg)

                }
            }
            .addOnFailureListener { exception ->

            }


        //수정하기
        binding.btBoardEditEdit.setOnClickListener {

            val storage = Firebase.storage
            val newTitle = binding.tvBoardEditTitle.text.toString()
            val newMsg = binding.tvBoardEditMsg.text.toString()

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "제목을 입력해 주세요", Toast.LENGTH_LONG).show()
            } else if (newMsg.isEmpty()) {
                Toast.makeText(this, "내용을 입력해 주세요", Toast.LENGTH_LONG).show()
            } else {

                //제목 or 내용만 수정 할 경우 -> 삭제사진 리스트, 추가 사진리스트 =0
                if (deleteImageList.size == 0 && addImageList.size == 0) {
                    db.collection("Board").document(boardKey).update("title", newTitle)
                    db.collection("Board").document(boardKey).update("msg", newMsg)
                    finish()
                }
                //사진 한 장이라도 수정되는 경우
                else {

                    //스토리지 삭제로직
                    for (deleteItem in deleteImageList) {
                        try {
                            storage.getReferenceFromUrl(deleteItem.toString()).delete()
                                .addOnSuccessListener { Log.d("targetlog", "2") }

                        } catch (e: Exception) {
                            Log.d("targetlog", "3")
                        }
                    }

                    //핸들러에게 시킬 마무리 동작
                    val handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            db.collection("Board").document(boardKey).update("title", newTitle)
                            db.collection("Board").document(boardKey).update("msg", newMsg)
                            db.collection("Board").document(boardKey)
                                .update("imageCount", imageList.size)
                            db.collection("Board").document(boardKey)
                                .update("imageNameList", imageListString)
                            Thread.sleep(750)
                            binding.pbUploadLoading2.isVisible = false
                            finish()
                        }
                    }

                    val storage = Firebase.storage

                    //image 한개라도 있으면

                    //기존 사진 존재 확인
                    if (imageListStringFromRead != null) {
                        for (item in imageListStringFromRead) {
                            try {
                                storage.reference.child("BoardImage").child(boardKey)
                                    .child("${item}.image").downloadUrl.addOnSuccessListener {
                                        imageListString.add(item.toString())
                                    }
                            } catch (e: Exception) {
                            }

                        }


                    }

                    binding.pbUploadLoading2.isVisible = true

                    //사진 스토리지에 업로드, 다운로드 받아서 확인
                    thread(start = true) {
                        for (image in addImageList) {
                            val imageReplace = image.toString().replace("/", "")
                            val storageRef =
                                storage.reference.child("BoardImage").child(boardKey)
                                    .child("${imageReplace}.image")


                            imageListString.add(imageReplace)
                            val uploadTask = storageRef.putFile(image)

                            val urlTask = uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                storageRef.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    imageListCheck.add(task.result)
                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }

                        }


                        //iamgeList는 사진 올리기 전에 정해짐 -> 업로드(시간 걸림) -> 올릴 때 마다 ImageListCheck에 하나식 uri추가 -> imageList랑 ImageListCheck랑 사이즈 같은 지 확인
                        //사이즈가 다르면 0.5초 쉬고 다시 확인
                        while (true) {
                            if (addImageList.size != imageListCheck.size) {
                                Thread.sleep(500)
                            } else {


                                handler?.sendEmptyMessage(0)

                                break
                            }
                        }


                    }


                }
            }
        }

    }

    //사진 추가 최대 10
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //imageCount


        if (resultCode == RESULT_OK && requestCode == 200) {
            if (imageList.size <= 10) {


                if (data?.clipData != null) { // 사진 여러개 선택한 경우
                    var count = data.clipData!!.itemCount

                    if (10 - imageList.size - count < 0) {
                        Toast.makeText(applicationContext,
                            "최대 ${10 - imageList.size}개 까지 더 선택 가능합니다.",
                            Toast.LENGTH_LONG).show()
                        return
                    } else {
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            imageList.add(imageUri)
                            //////////////////////////////
                            addImageList.add(imageUri)
                            deleteImageList.remove(imageUri)
                            //////////////////////////////

                            // 이미지 개수
                            binding.tvImageCount2.text = imageList.size.toString()
                        }
                    }

                } else { // 단일 선택
                    data?.data?.let { uri ->
                        val imageUri: Uri? = data?.data
                        if (imageUri != null) {
                            imageList.add(imageUri)
                            //////////////////////////////
                            addImageList.add(imageUri)
                            deleteImageList.remove(imageUri)
                            //////////////////////////////
                            binding.tvImageCount2.text = imageList.size.toString()
                        }
                    }
                }
                multiImageAdapter.notifyDataSetChanged()

            } else {
                Toast.makeText(applicationContext, "사진은 최대 10장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }
}