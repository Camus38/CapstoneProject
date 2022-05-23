package com.hansung2022.capstoneday1zipkok

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.icu.text.SimpleDateFormat
import android.location.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.Auth.LoginActivity
import com.hansung2022.capstoneday1zipkok.Data.*
import com.hansung2022.capstoneday1zipkok.Fragments.BoardFragment
import com.hansung2022.capstoneday1zipkok.Fragments.HomeFragment
import com.hansung2022.capstoneday1zipkok.Fragments.TipFragment
import com.hansung2022.capstoneday1zipkok.Fragments.TodoFragment
import com.hansung2022.capstoneday1zipkok.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import retrofit2.Call
import retrofit2.Response
import java.io.InputStream
import java.lang.Exception
import java.lang.Runnable
import javax.xml.parsers.DocumentBuilderFactory

var text = "시험용"
var dust : ModelDust = ModelDust()
var locate = ""
var currentTime = System.currentTimeMillis()
//var latitude : String = "37.4927722222"
var latitude : String? = null
//var longitude : String = "127.123911111"
var longitude : String? = null


var locationName : String = ""
var GpsTranslater = GpsTranslater()




class MainActivity : AppCompatActivity() {
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mLocationRequest: LocationRequest
    lateinit var mLastLocation: Location
    private val REQUEST_PERMISSION_LOCATION = 10
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    var weatherarr = arrayOf(ModelWeather())

    @RequiresApi(Build.VERSION_CODES.N)
    var date = Getdate(currentTime)
    @RequiresApi(Build.VERSION_CODES.O)
    var time = Gettime(currentTime)


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        weatherarr[0].rainType = "데이터 없음"
        weatherarr[0].humidity = "데이터 없음"
        weatherarr[0].sky = "데이터 없음"
        weatherarr[0].temp = "데이터 없음"
        locationName = "알수없음"



        mLocationRequest =  LocationRequest.create().apply {
            fastestInterval = 0
            interval = 0
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }






        CoroutineScope(Dispatchers.Main).launch {
            setLocation()
        }







        replacement(1)

        //#########Fragment 화면전환
        binding.btBoard.setOnClickListener { replacement(1) }
        binding.btHome.setOnClickListener { replacement(2) }
        binding.btTodo.setOnClickListener { replacement(3) }
        binding.btTip.setOnClickListener { replacement(4) }





        // Initialize Firebase Auth
        auth = Firebase.auth

        registerForContextMenu(binding.ivMenu)

    }

    fun setLocation(){
        var currentLocation : String = "현재 위치"
        var geocoder = Geocoder(this)
        var resultList : List<Address>? = null
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try{

            checkPermissionForLocation(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }

            mFusedLocationProviderClient.lastLocation.addOnSuccessListener {

                latitude = it.latitude.toString()
                longitude = it.longitude.toString()

                Log.d("locate","$latitude,$longitude")

                try {
                    //latitude = "37.4927722222"
                    //longitude = "127.123911111"
                    resultList = geocoder.getFromLocation(latitude!!.toDouble(), longitude!!.toDouble(),1)
                    Log.d("Location","location success")
                } catch (e: Exception){
                    e.printStackTrace()
                }
                if(resultList != null){
                    currentLocation = resultList!![0].getAddressLine(0)
                    locationName = currentLocation.substring(5)
                    locate = currentLocation.substring(5,10)
                    getlocate(locate)
                    setDust(locate)
                    setWeather(latitude!!, longitude!!)
                    Log.d("locate", currentLocation)
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
            Log.d("Fail","위치 정보 얻기 실패")
        }


    }




    fun setWeather(nx : String, ny : String) {
        GpsTranslater.GpsTransfer(nx.toDouble(),ny.toDouble())
        GpsTranslater.xyconverter(GpsTranslater,0)

        //일기예보 base_time이 31..59분사이로 입력되어야 하므로 현재시간을 31..59분 사이로 맞춘다.
        if (time.toInt()%100 in 0..29) {
            var s = time.toInt()-70
            time = s.toString()
        }


        val call = ApiObject.retrofitService.GetWeather(60, 1, "JSON", date, time, GpsTranslater.xlat.toString(), GpsTranslater.ylon.toString())

        call.enqueue(object : retrofit2.Callback<WEATHER>{

            override fun onResponse(call : Call<WEATHER>, response: Response<WEATHER>){
                if(response.isSuccessful){

                    if(response.body()!!.response.body == null){
                        var weatherArr = arrayOf(ModelWeather())
                        weatherArr[0].rainType = "데이터 없음"
                        weatherArr[0].humidity = "데이터 없음"
                        weatherArr[0].sky = "데이터 없음"
                        weatherArr[0].temp = "데이터 없음"

                        weatherarr = weatherArr
                        Log.d("Sccess", "weather")
                    }else {

                        val it: List<ITEM> = response.body()!!.response.body.items.item

                        var weatherArr = arrayOf(
                            ModelWeather(),
                            ModelWeather(),
                            ModelWeather(),
                            ModelWeather(),
                            ModelWeather(),
                            ModelWeather()
                        )

                        var index = 0
                        val totalCount = response.body()!!.response.body.totalCount - 1
                        for (i in 0..totalCount) {
                            index %= 6
                            when (it[i].category) {
                                "PTY" -> weatherArr[index].rainType = it[i].fcstValue
                                "REH" -> weatherArr[index].humidity = it[i].fcstValue
                                "SKY" -> weatherArr[index].sky = it[i].fcstValue
                                "T1H" -> weatherArr[index].temp = it[i].fcstValue
                                else -> continue
                            }
                            index++
                        }
                        weatherarr = weatherArr
                        Log.d("Sccess", "weather")
                    }

                }
            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                Log.d("fall","$t")
            }
        })
    }

    fun setDust(loca : String){

        val call = ApiObject_dust.retrofitService.GetDust(loca,1,10,"json","1.0")

        call.enqueue(object : retrofit2.Callback<DUST>{

            override fun onResponse(call: Call<DUST>, response: Response<DUST>) {

                if(response.isSuccessful){

                    if(response.body()!!.response.body == null){

                        var dusts = ModelDust()

                        dusts.value = "Not Have Data"
                        dust = dusts
                        Log.d("Dust", "dust")

                    }else if(response.body()!!.response.body.totalCount == 0){

                        var dusts = ModelDust()

                        dusts.value = "Not Have Data"
                        dust = dusts
                        Log.d("Dust", "dust")

                    }else {
                        val it: List<ITEMS_DUST> = response.body()!!.response.body.items

                        var dusts = ModelDust()

                        val totalCount = response.body()!!.response.body.totalCount - 30
                        var i = 0
                        if (it.size == 0) {

                            when (it[i].pm10Grade) {
                                "1" -> dusts.value = it[i].pm10Grade
                                "2" -> dusts.value = it[i].pm10Grade
                                "3" -> dusts.value = it[i].pm10Grade
                                "4" -> dusts.value = it[i].pm10Grade
                            }
                        } else {
                            when (it[i].pm10Grade) {
                                "1" -> dusts.value = it[i].pm10Grade
                                "2" -> dusts.value = it[i].pm10Grade
                                "3" -> dusts.value = it[i].pm10Grade
                                "4" -> dusts.value = it[i].pm10Grade
                            }
                        }
                        dust = dusts
                        Log.d("Dust", "dust")
                    }
                }
            }

            override fun onFailure(call: Call<DUST>, t: Throwable) {
                Log.d("fall","$t")
            }
        })
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }








    //현재시간 데이터를 HomeFragment로 보내는 함수
    @RequiresApi(Build.VERSION_CODES.N)
    fun Gettime(current : Long) : String{
        val stf = SimpleDateFormat("HHmm")
        return stf.format(current)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun Getdate(current : Long) : String{
        val sdf = SimpleDateFormat("yyyyMMdd")
        return sdf.format(current)
    }

    fun getLocationName() :String{
        return locationName
    }


    //날씨데이터를 HomeFragment 연결
    fun getList() : Array<ModelWeather> {
        return weatherarr
    }
    //미세먼지 데이터를 HomeFragment 연결
    fun getDustInfo() : ModelDust{
        return dust
    }


    ///뒤로가기 두 번 눌러서 종료 2초 텀
    var mBackWait: Long = 0

    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackWait >= 2000) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

    //#########Fragment 화면전환
    fun replacement(number: Int) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            when (number) {
                1 -> replace<BoardFragment>(R.id.main_my_fragment)
                2 -> replace<HomeFragment>(R.id.main_my_fragment)
                3 -> replace<TodoFragment>(R.id.main_my_fragment)
                4 -> replace<TipFragment>(R.id.main_my_fragment)
                else -> return@commit
            }
            if(number == 2){
                val loading = LoadingDialog(this@MainActivity)
                loading.startLoading()
                val handler = Handler()
                handler.postDelayed(object : Runnable{
                    override fun run() {
                        loading.isDismiss()
                    }
                },2000)
            }
            addToBackStack(null)
        }

    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.main_option, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.userinfo -> {
                val intent = Intent(this, UserInfoActivity::class.java)
                startActivity(intent)
            }

            R.id.logout -> {
                AlertDialog.Builder(this)
                    .setTitle("로그아웃")
                    .setMessage("정말 로그아웃 하시겠습니까?")
                    .setNegativeButton("로그아웃"){_,_->
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setPositiveButton("취소"){_,_->}
                    .create()
                    .show()
            }
        }
        return super.onContextItemSelected(item)
    }



}


fun getlocate(loca: String){
    if(loca == "서울특별시")
        locate = "서울"
    else if(loca == "부산광역시")
        locate = "부산"
    else if(loca == "대구광역시")
        locate = "대구"
    else if(loca == "인천광역시")
        locate = "인천"
    else if(loca == "광주광역시")
        locate = "광주"
    else if(loca == "대전광역시")
        locate = "대전"
    else if(loca == "울산광역시")
        locate = "울산"
    else if(loca == "전라북도")
        locate = "전북"
    else if(loca == "전라남도")
        locate = "전남"
    else if(loca == "경상북도")
        locate = "경북"
    else if(loca == "경상남도")
        locate = "경남"
    else if(loca == "경기도")
        locate = "경기"
    else if(loca =="충청북도")
        locate = "충북"
    else if(loca =="충청남도")
        locate = "충남"
    else if(loca == "세종특별자치시")
        locate = "세종"
    else if(loca == "제주특별자치도")
        locate = "제주"

}



