package com.hansung2022.capstoneday1zipkok.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.MainActivity
import com.hansung2022.capstoneday1zipkok.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //로그인 하기
        binding.btLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPw.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this,"로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //회원가입으로 이동
        binding.btEnrollment.setOnClickListener {
            val intent = Intent(this, EnrollmentActivity::class.java)
            startActivity(intent)
            finish()
        }

    }




    ///뒤로가기 두 번 눌러서 종료 2초 텀
    var mBackWait:Long = 0

    override fun onBackPressed() {
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

}