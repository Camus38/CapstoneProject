package com.hansung2022.capstoneday1zipkok.BoardImage

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.bumptech.glide.Glide
import com.hansung2022.capstoneday1zipkok.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val getUri = intent.getParcelableExtra<Uri>("imageUri")
        Log.d("myUri", getUri.toString())


        Glide.with(this).load(getUri)
            .override(2500,2500)
            .into(binding.ivGallery)

        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {

        // 제스처 이벤트를 처리하는 메소드를 호출
        mScaleGestureDetector!!.onTouchEvent(motionEvent)
        return true


    }

    // 제스처 이벤트를 처리하는 클래스
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {

            scaleFactor *= scaleGestureDetector.scaleFactor

            // 최소 0.5, 최대 2배
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f))

            // 이미지에 적용
            binding.ivGallery.scaleX = scaleFactor
            binding.ivGallery.scaleY = scaleFactor
            return true
        }
    }
}