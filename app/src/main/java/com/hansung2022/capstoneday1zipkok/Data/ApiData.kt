package com.hansung2022.capstoneday1zipkok.Data



import androidx.room.*
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherInterface{
    @GET("getUltraSrtFcst?serviceKey=lPtJ%2BHQfZafMKSK1KFkwGrTV4GZRnmBMPsUGcCIPR0tyDS5VEHvZv86KY9ICm3wTlPpJXlridmrwcfJ%2F1o%2Bjxw%3D%3D")

    fun GetWeather(@Query("numOfRows") num_of_rows : Int,
                   @Query("pageNo") page_no : Int,
                   @Query("dataType") data_type : String,
                   @Query("base_date") base_date : String,
                   @Query("base_time") base_time : String,
                   @Query("nx") nx : String,
                   @Query("ny") ny : String
    ) : Call<WEATHER>
}

@Entity
data class NationalWeatherTable(
    @PrimaryKey var code : Long,
    var name1 : String,
    var name2 : String,
    var name3 : String,
    var gridx : Int,
    var gridy : Int,
    var y : Double,
    var x : Double
)


@Dao
interface NationalWeatherInterface{

    @androidx.room.Query("SELECT * FROM NationalWeatherTable")
    suspend fun getAll(): List<NationalWeatherTable>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nationalWeatherTable: NationalWeatherTable)

    @androidx.room.Query("DELETE FROM NationalWeatherTable")
    suspend fun deleteAll()

    @androidx.room.Query("SELECT * FROM NationalWeatherTable WHERE (x BETWEEN :x-0.01 AND :x+0.01) AND (y BETWEEN :y-0.01 AND :y+0.01)")
    suspend fun search(x: Double,y: Double) : List<NationalWeatherTable>
}



@Database(entities = [NationalWeatherTable::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun nationalWeatherInterface(): NationalWeatherInterface
}

interface DustInterface{
    @GET("getCtprvnRltmMesureDnsty?serviceKey=lPtJ%2BHQfZafMKSK1KFkwGrTV4GZRnmBMPsUGcCIPR0tyDS5VEHvZv86KY9ICm3wTlPpJXlridmrwcfJ%2F1o%2Bjxw%3D%3D")
    fun GetDust(@Query("sidoName") sidoName: String,
                @Query("pageNo") page_no: Int,
                @Query("numOfRows") num_of_rows: Int,
                @Query("returnType") return_type: String,
                @Query("ver") ver: String
    ) : Call <DUST>
}

data class WEATHER(val response : RESPONSE)
data class RESPONSE(val header : HEADER, val body : BODY)
data class HEADER(val resultCode : Int, val resultMsg : String)
data class BODY(val dataType : String, val items : ITEMS, val totalCount : Int)
data class ITEMS(val item : List<ITEM>)
// fcstDate : 예측날짜  , fcstTime : 예측시간  , fcstValue : 예측값
data class ITEM(val category : String,val basetime : String, val fcstDate : String, val fcstTime : String, val fcstValue : String)

data class DUST(val response : RESPONSE_DUST)
data class RESPONSE_DUST(val body: BODY_DUST, val header : HEADER_DUST)
data class HEADER_DUST(val resultCode : Int, val resultMsg : String)
data class BODY_DUST(val totalCount: Int, var items : List<ITEMS_DUST>)
data class ITEMS_DUST(var pm10Grade : String)



val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(100, TimeUnit.SECONDS)
    .readTimeout(100, TimeUnit.SECONDS)
    .writeTimeout(100, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val retrofit_dust = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object ApiObject_dust{
    val retrofitService : DustInterface by lazy{
        retrofit_dust.create(DustInterface ::class.java)
    }
}

object ApiObject {
    val retrofitService : WeatherInterface by lazy{
        retrofit.create(WeatherInterface ::class.java)
    }
}
