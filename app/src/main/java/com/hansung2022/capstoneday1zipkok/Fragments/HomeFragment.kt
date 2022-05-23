package com.hansung2022.capstoneday1zipkok.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.hansung2022.capstoneday1zipkok.Data.LoadingDialog
import com.hansung2022.capstoneday1zipkok.Data.ModelDust
import com.hansung2022.capstoneday1zipkok.Data.ModelWeather
import com.hansung2022.capstoneday1zipkok.MainActivity
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception
import java.lang.Runnable

class HomeFragment : Fragment() {

    private lateinit var name: String
    private lateinit var degree: String
    private lateinit var sky: String
    private lateinit var dust: ModelDust
    private var binding: FragmentHomeBinding? = null
    private lateinit var weather: Array<ModelWeather>
    private lateinit var locationName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        Log.d("HomeFragment", "onAttach")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val activity = context as MainActivity

        val Main = context as MainActivity

        dust = Main.getDustInfo()
        weather = Main.getList()
        locationName = Main.getLocationName()

        if (Main.weatherarr[0].rainType.equals("데이터 없음")) {
            Toast.makeText(context, "정보가 없습니다 새로고침 해주세요", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(context, "정보를 불러왔습니다.", Toast.LENGTH_SHORT).show()
        }



        binding!!.button.setOnClickListener {

            val loading = LoadingDialog(activity)
            loading.startLoading()
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    loading.isDismiss()
                }
            }, 3000)

            val job = CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                refreshFragment()
            }

            runBlocking {
                val main = context as MainActivity
                main.setWeather("37.492772222", "127.123911111")
                main.setDust("서울")
                //main.setLocation()
                setData()
                job

            }


        }

        Log.d("HomeFragment", "onCreateView")
        setData()


        return binding!!.root
    }

    fun setData() {

        if (weather[0].humidity == "데이터 없음")
            binding!!.humidity.text = "데이터 없음"
        else
            binding!!.humidity.text = "습도 : " + weather[0].humidity + "%"


        if (weather[0].temp == "데이터 없음")
            binding!!.degree.text = "데이터 없음"
        else
            binding!!.degree.text = "온도 :" + weather[0].temp + "°"

        when (dust.value) {
            "1" -> binding!!.dust.text = "미세먼지 : 낮음"
            "2" -> binding!!.dust.text = "미세먼지 : 보통"
            "3" -> binding!!.dust.text = "미세먼지 : 높음"
            "4" -> binding!!.dust.text = "미세먼지 : 매우높음"
            "Not Have Data" -> binding!!.dust.text = "데이터 없음"
        }

        when (weather[0].sky) {
            "1" -> binding!!.sky.text = "날씨 : 맑음"
            "3" -> binding!!.sky.text = "날씨 : 구름많음"
            "4" -> binding!!.sky.text = "날씨 : 흐림"
            "데이터 없음" -> binding!!.sky.text = "데이터 없음"
        }

        when (binding!!.sky.text) {
            "날씨 : 구름많음" -> binding!!.weather.setImageResource(R.drawable.cloud)
            "날씨 : 맑음" -> binding!!.weather.setImageResource(R.drawable.sunny)
            "날씨 : 흐림" -> binding!!.weather.setImageResource(R.drawable.blur)
        }

        binding!!.LocationTv.text = locationName

        if (weather[0].temp == "데이터 없음") {
            binding!!.outfitCoat.text = "외투 : X"
            binding!!.outfitTop.text = "상의 : X"
            binding!!.outfitPants.text = "하의 : X"

        } else {
            if (weather[0].temp.toInt() >= 28) {
                binding!!.outfitCoat.text = "외투 : X"
                binding!!.outfitTop.text = "상의 : 민소매,반팔"
                binding!!.outfitPants.text = "하의 : 반바지,원피스"
                binding!!.outfitImage.setImageResource(R.drawable.outfit1)
            } else if (weather[0].temp.toInt() in 23..27) {
                binding!!.outfitCoat.text = "외투 : X"
                binding!!.outfitTop.text = "상의 : 얇은 셔츠,반팔"
                binding!!.outfitPants.text = "하의 : 반바지,면바지"
                binding!!.outfitImage.setImageResource(R.drawable.outfit2)
            } else if (weather[0].temp.toInt() in 20..22) {
                binding!!.outfitCoat.text = "외투 : 얇은 가디건"
                binding!!.outfitTop.text = "상의 : 긴팔,반팔"
                binding!!.outfitPants.text = "하의 : 면바지,청바지"
                binding!!.outfitImage.setImageResource(R.drawable.outfit3)
            } else if (weather[0].temp.toInt() in 17..19) {
                binding!!.outfitCoat.text = "외투 : 가디건"
                binding!!.outfitTop.text = "상의 : 얇은 니트,맨투맨"
                binding!!.outfitPants.text = "하의 : 청바지,면바지"
                binding!!.outfitImage.setImageResource(R.drawable.outfit4)
            } else if (weather[0].temp.toInt() in 12..16) {
                binding!!.outfitCoat.text = "외투 : 자켓,가디건,야상"
                binding!!.outfitTop.text = "상의 : 긴팔,맨투맨"
                binding!!.outfitPants.text = "하의 : 스타킹,청바지,면바지"
                binding!!.outfitImage.setImageResource(R.drawable.outfit5)
            } else if (weather[0].temp.toInt() in 9..11) {
                binding!!.outfitCoat.text = "외투 : 자켓,트렌치코트,야상"
                binding!!.outfitTop.text = "상의 : 니트,맨투맨,후드"
                binding!!.outfitPants.text = "하의 : 청바지,스타킹"
                binding!!.outfitImage.setImageResource(R.drawable.outfit6)
            } else if (weather[0].temp.toInt() in 5..8) {
                binding!!.outfitCoat.text = "외투 : 코트,가죽자켓"
                binding!!.outfitTop.text = "상의 : 후드,니트,맨투맨"
                binding!!.outfitPants.text = "하의 : 레깅스,스타킹,청바지"
                binding!!.outfitImage.setImageResource(R.drawable.outfit7)
            } else if (weather[0].temp.toInt() <= 4) {
                binding!!.outfitCoat.text = "외투 : 패딩,두꺼운 코드"
                binding!!.outfitTop.text = "상의 : 니트,기모제품"
                binding!!.outfitPants.text = "하의 : 청바지,스타킹"
                binding!!.outfitImage.setImageResource(R.drawable.outfit8)
            }
        }


    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "onresume")
        //(context as MainActivity).setLocation()
        //setData()

    }

    override fun onStart() {
        super.onStart()
        Log.d("HomeFragment", "onstart")
    }

    override fun onPause() {
        super.onPause()
        Log.d("HomeFragment", "onpause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("HomeFragment", "onstop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HomeFragment", "ondestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "ondestroyview")

        binding = null
    }

    fun refreshFragment() {
        val mainActivity = (activity as MainActivity)
        mainActivity.supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<HomeFragment>(R.id.main_my_fragment)
        }

        Log.d("Fragment", "Replace")
    }



}
