package com.hansung2022.capstoneday1zipkok

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.Auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //로그인 상태가 아니라면 LoginActivity로 이동, 로그인 상태라면 메인 액티비티로 이동
        if(auth.currentUser?.uid==null){
            //1.5초 후 실행후 SplashActivity 종료
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 1500)
        }

        //로그인 상태라면 바로 메인으로
        else{
            //1.5초 후 실행후 SplashActivity 종료
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1500)
        }


    }
}