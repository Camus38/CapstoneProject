package com.hansung2022.capstoneday1zipkok.Data

import android.util.Log
import kotlin.properties.Delegates

class GpsTranslater {

    var lat : Double? = null // 위도
    var lon : Double? = null // 경도
    var xlat : Int? = null //격자 x
    var ylon : Int? = null //격자 y

    fun GpsTransfer(){}

    fun GpsTransfer(lat : Double, lon : Double){
        this.lat = lat
        this.lon = lon
    }

    fun getLat() : Double {
        return lat!!
    }

    fun getLon() : Double{
        return lon!!
    }


    fun setXLat(xLat : Int){
        this.xlat = xLat
    }

    fun setYLon(yLon : Int){
        this.ylon = yLon
    }



    fun xyconverter(gpt : GpsTranslater, mode : Int){
        var RE = 6471.00877 //지구의 반경(KM)
        var GRID = 5.0 //격자 간격(KM)
        var SLAT1 = 30.0 //투영 위도1
        var SLAT2 = 60.0 //투영 위도2
        var OLON = 126.0 // 기준점 경도
        var OLAT = 38.0 //기준점 위도
        var XO = 43 //기준점 x좌표
        var YO = 136

        var DEGRAD = Math.PI / 180.0


        var re = RE / GRID
        var slat1 = SLAT1 * DEGRAD
        var slat2 = SLAT2 * DEGRAD
        var olon = OLON * DEGRAD
        var olat = OLAT * DEGRAD


        var sn : Double = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)

        var sf : Double = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn

        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        if (mode == 0){
            var ra : Double = Math.tan(Math.PI * 0.25 + (gpt.getLat()) * DEGRAD * 0.5)
            ra = re * sf / Math.pow(ra, sn)

            var theta : Double = gpt.getLon() * DEGRAD - olon

            if (theta > Math.PI)
                theta -= 2.0 * Math.PI
            if (theta < -Math.PI)
                theta += 2.0 * Math.PI

            theta *= sn

            var x : Int = Math.floor(ra * Math.sin(theta) + XO + 0.5).toInt()
            var y : Int = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5).toInt()

            gpt.setXLat(x)
            gpt.setYLon(y)
            Log.d("GPSTRANSFER","$xlat,$ylon")
        }
    }
}