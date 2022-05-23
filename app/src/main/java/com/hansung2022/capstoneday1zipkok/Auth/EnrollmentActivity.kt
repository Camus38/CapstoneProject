package com.hansung2022.capstoneday1zipkok.Auth

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.MainActivity
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.ActivityEnrollmentBinding

//회원가입 정보 데이터 클래스
data class UserInfo(
    var email: String? = null,
    var pw1: String? = null,
    var nickname: String? = null,
    var uid: String? = null,
)

class EnrollmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnrollmentBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEnrollmentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        ////////////////
        val db = Firebase.firestore


        //닉네임 중복 확인
        binding.btNicknameCheck.setOnClickListener {
            var usuableNickname: Boolean = true
            val nickname = binding.etEnrollemntNickname.text.toString()

            if (nickname.length < 4 || nickname.length > 11) {
                Toast.makeText(this, "닉네임은 5자리~10자리만 사용가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("UserInfo").get().addOnSuccessListener { documnets ->
                    for (document in documnets) {
                        val existNickname = document["nickname"].toString()
                        if (existNickname == binding.etEnrollemntNickname.text.toString()) {

                            usuableNickname = false
                        }

                    }
                    if (usuableNickname == true) {
                        binding.btNicknameCheck.text = "사용가능"
                        binding.btNicknameCheck.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.my_green))
                        binding.etEnrollemntNickname.isEnabled = false

                        //binding.btNicknameCheckCancel.isVisible = true
                    } else {
                        binding.btNicknameCheck.text = "중복확인"
                        Toast.makeText(this, "해당 닉네임 사용불가", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //닉네임 재입력 하기
        binding.btNicknameCheckCancel.setOnClickListener {
            binding.btNicknameCheck.text = "중복확인"
            binding.btNicknameCheck.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.my_main_color))
            binding.etEnrollemntNickname.setText("")
            binding.etEnrollemntNickname.isEnabled = true

            //binding.btNicknameCheckCancel.isVisible = false
        }


        //회원가입
        binding.btEnrollment.setOnClickListener {
            val email: String = binding.etEnrollemntEmail.text.toString()
            val pw1 = binding.etEnrollmentPw1.text.toString()
            val pw2 = binding.etEnrollemntPw2.text.toString()
            val nickname = binding.etEnrollemntNickname.text.toString()
            var check: Boolean = true //아래 제약사항에 걸리지 않으면 true -> 회원가입 시킴
            val nicknameCheck = binding.btNicknameCheck.text.toString()


            //이메일 값이 입력되지 않았을 경우
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show()
                check = false
            }
            // 비밀번호가 입력되지 않았을 경우
            else if (pw1.isEmpty()) {
                Toast.makeText(this, "비밀번호 입력하세요", Toast.LENGTH_SHORT).show()
                check = false
            }
            // 비밀번호 재입력이 입력되지 않았을 경우
            else if (pw2.isEmpty()) {
                Toast.makeText(this, "비밀번호를 재입력하세요", Toast.LENGTH_SHORT).show()
                check = false
            }
            //비밀번호가 재입력과 다를 경우
            else if (!pw1.equals(pw2)) {
                Toast.makeText(this, "비밀번호가 서로 다릅니다", Toast.LENGTH_SHORT).show()
                check = false
            }
            //비밀번호가 6자리 미만일 경우
            else if (pw2.length < 6) {
                Toast.makeText(this, "비밀번호를 6자리 이상 입력하세요", Toast.LENGTH_SHORT).show()
                check = false
                //닉네임 미입력시
            } else if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                check = false
                //닉네임 길이 5~14자
            } else if (nickname.length < 4 || nickname.length > 11) {
                Toast.makeText(this, "닉네임을 5글자 이상 10글자 이하 입력하세요", Toast.LENGTH_SHORT).show()
                check = false
            } else if (nicknameCheck != "사용가능") {
                Toast.makeText(this, "닉네임 중복확인 하세요", Toast.LENGTH_SHORT).show()
                check = false
            }
            if (check == true) {
                auth.createUserWithEmailAndPassword(email, pw1)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {


                            val uid = auth.currentUser!!.uid


                            val userInfoList = UserInfo(email, pw1, nickname, uid)
                            db.collection("UserInfo").document(uid).set(userInfoList)


                            Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()


                        } else {
                            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

    }

    //회원가입 액티비티에서 뒤로가기 버튼 누르면 Login 액티비티로 열기
    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}