package com.hansung2022.capstoneday1zipkok

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hansung2022.capstoneday1zipkok.databinding.ActivityUserInfoBinding
import java.lang.Exception
import java.lang.NullPointerException

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var myProfileImage: Uri


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        val db = Firebase.firestore
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid
        val storage = Firebase.storage
        val storageRef = storage.reference

        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val docRef = db.collection("UserInfo").document(uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val email = document["email"].toString()
                    val nickname = document["nickname"].toString()

                    binding.tvUserinfoEmail.text = email
                    binding.tvUserinfoNickname.text = nickname
                } else {

                }
            }
            .addOnFailureListener { exception ->

            }


        initAddPhotoButton()

        //프로필 사진 받아오기
        getImageFromFireBase()

        //사진 업로드
        binding.btProfileUploadToFB.setOnClickListener {
            try {
                binding.pbUserInfo.isVisible = true
                uploadImageToFirebase()

            } catch (e: Exception) {
                binding.pbUserInfo.isVisible = false
                Toast.makeText(this, "사진이 없습니다.", Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun getImageFromFireBase() {
        val storage = Firebase.storage
        val storageRef = storage.reference
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        try {
            storageRef.child("UserProfile").child(uid)
                .child("0.jpg").downloadUrl.addOnSuccessListener {

                Glide.with(this)
                    .load(it)
                    .override(200, 200)
                    .into(binding.ivProfile)

                binding.pbUserInfo.isVisible = false

            }.addOnFailureListener {
                // Handle any errors
                binding.pbUserInfo.isVisible = false
            }

        } catch (e: NullPointerException) {
            binding.pbUserInfo.isVisible = false
        }

    }


    private fun uploadImageToFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val db = Firebase.firestore
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        val ref = storageRef.child("UserProfile").child(uid).child("0.jpg")
        ref.putFile(myProfileImage).addOnSuccessListener {
            Toast.makeText(this, "이미지 변경 완료", Toast.LENGTH_SHORT).show()
            binding.pbUserInfo.isVisible = false
        }


    }

    //갤러리 권한 받기
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initAddPhotoButton() {
        binding.btChangeImage.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    navigatePhotos()

                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000)
                }

            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    navigatePhotos()

                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //
            }
        }
    }

    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            2000 -> {
                val selectedImageUri: Uri? = data?.data

                if (selectedImageUri != null) {

                    //파이어 베이스에 업로드할 때 사용
                    myProfileImage = selectedImageUri

                    //이미지뷰에 일단 적용
                    binding.ivProfile.setImageURI(selectedImageUri)

                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }


            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
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
}