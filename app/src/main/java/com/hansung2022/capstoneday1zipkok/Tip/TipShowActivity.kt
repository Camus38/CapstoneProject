package com.hansung2022.capstoneday1zipkok.Tip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.hansung2022.capstoneday1zipkok.R

class TipShowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_show)

        val getUrl = intent.getStringExtra("url")

        val webView = findViewById<WebView>(R.id.myWebView)
        webView.loadUrl(getUrl.toString())
        finish()
    }
}