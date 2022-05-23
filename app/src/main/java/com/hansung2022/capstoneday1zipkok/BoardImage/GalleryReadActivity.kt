package com.hansung2022.capstoneday1zipkok.BoardImage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.bumptech.glide.Glide
import com.hansung2022.capstoneday1zipkok.databinding.ActivityGalleryReadBinding

class GalleryReadActivity : AppCompatActivity() {
    private lateinit var binding:ActivityGalleryReadBinding
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityGalleryReadBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val getUri = intent.getStringExtra("imageUri")
        Log.d("myUri", getUri.toString())

        Glide.with(this).load(getUri)
            .override(2500,2500)
            .into(binding.ivGalleryRead)

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
            binding.ivGalleryRead.scaleX = scaleFactor
            binding.ivGalleryRead.scaleY = scaleFactor
            return true
        }
    }
}