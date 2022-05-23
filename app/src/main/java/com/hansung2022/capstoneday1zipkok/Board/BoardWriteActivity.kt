package com.hansung2022.capstoneday1zipkok.Board

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.hansung2022.capstoneday1zipkok.databinding.ActivityBoardWriteBinding
import kotlin.concurrent.thread

class BoardWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardWriteBinding
    private lateinit var auth: FirebaseAuth

    //이미지 업로드용
    var imageList: MutableList<Uri> = mutableListOf()
    var imageListCheck: MutableList<Uri> = mutableListOf()
    var imageListString : MutableList<String> = mutableListOf()
    val multiImageAdapter = MultiImageAdapter(imageList, this)
    private lateinit var imageCount: TextView

    //스토리지로 부터 다운로드 받은 주소 imageList랑 확실히 다름<얘는 파이어베이스에 저장된 주>
    var imageListDownload: MutableList<String> = mutableListOf()



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityBoardWriteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAddPhotoButton()




        // 리사이클러뷰
        var recyclerview = findViewById<RecyclerView>(R.id.rv_imageAdd)




        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerview.layoutManager = layoutManager
        recyclerview.adapter = multiImageAdapter

        multiImageAdapter.itemClick = object : MultiImageAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                imageList.removeAt(position)
                multiImageAdapter.notifyDataSetChanged()
                imageCount.text = imageList.size.toString()
            }
        }
        //이미지 눌러서 확대
        multiImageAdapter.imageClick = object : MultiImageAdapter.ImageClick {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(this@BoardWriteActivity, GalleryActivity::class.java)
                intent.putExtra("imageUri", imageList[position])
                startActivity(intent)
            }
        }





        auth = Firebase.auth
        val uid = auth.currentUser!!.uid //가져 왔고
        var nickname: String = "" //가져왔고

        val db = Firebase.firestore

        db.collection("UserInfo").document(uid)
            .get()
            .addOnSuccessListener { document ->
                nickname = document["nickname"] as String
            }





        binding.btUpload.setOnClickListener {

            var boardTitle = binding.etBoardTitle.text.toString()
            var boardMsg = binding.etBoardMsg.text.toString()
            val time = System.currentTimeMillis()
            //사진 올리기

            //제목, 내용 null check
            if (boardTitle.isEmpty()) {
                Toast.makeText(this, "제목을 입력해 주세요", Toast.LENGTH_LONG).show()
            } else if (boardMsg.isEmpty()) {
                Toast.makeText(this, "내용을 입력해 주세요", Toast.LENGTH_LONG).show()
            } else {

                val handler = object : Handler() {
                    override fun handleMessage(msg: Message) {

                        binding.pbUploadLoading.isVisible = false
                        finish()
                    }
                }
                //var ImageUploadCount = 0
                val storage = Firebase.storage

                //image 한개라도 있으면
                var imageCount = imageList.size

                if (imageList.size != 0) {
                    binding.pbUploadLoading.isVisible = true

                    thread(start = true) {
                        for (image in imageList) {

                            // 슬래시 때문에 하위 디렉터리로 넘어가서 /를 없애버림
                            val imageReplace = image.toString().replace("/", "")
                            val storageRef =
                                storage.reference.child("BoardImage").child(time.toString())
                                    .child("${imageReplace}.image")
                            //ImageUploadCount++
                            //추가
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
                        //사이즈가 다르면 1초 쉬고 다시 확인
                        while(true){
                            if(imageList.size != imageListCheck.size){
                                Thread.sleep(300)
                            }
                            else{
                                val boardReadModel = BoardReadModel(uid,
                                    nickname,
                                    boardTitle,
                                    boardMsg,
                                    time,
                                    imageCount,
                                    imageListString)
                                db.collection("Board").document("$time").set(boardReadModel)

                                handler?.sendEmptyMessage(0)
                                break
                            }
                        }


                    }
                    //이미지 없으면
                } else {
                    val boardReadModel = BoardReadModel(uid,
                        nickname,
                        boardTitle,
                        boardMsg,
                        time,
                        imageCount,
                        imageListString
                    )
                    db.collection("Board").document("$time").set(boardReadModel)
                    finish()
                }


            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initAddPhotoButton() {
        binding.ivGetImage.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    binding.ivGetImage.setOnClickListener {
                        val intent = Intent()
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.action = Intent.ACTION_PICK

                        startActivityForResult(intent, 200)
                    }
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 불러오기 위해 권한이 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.ivGetImage.setOnClickListener {
                        val intent = Intent()
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.action = Intent.ACTION_PICK

                        startActivityForResult(intent, 200)
                    }

                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //imageCount
        imageCount = binding.tvImageCount


        if (resultCode == RESULT_OK && requestCode == 200) {
            //5장까지 받기
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
                            // 이미지 개수
                            imageCount.text = imageList.size.toString()
                        }
                    }

                } else { // 단일 선택
                    data?.data?.let { uri ->
                        val imageUri: Uri? = data?.data
                        if (imageUri != null) {
                            imageList.add(imageUri)
                            imageCount.text = imageList.size.toString()
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