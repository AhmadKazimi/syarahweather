package com.kazimi.syarahweather.data.net.response

import com.google.gson.annotations.SerializedName
import com.kazimi.syarahweather.data.net.response.common.Coord
import com.kazimi.syarahweather.data.net.response.common.Weather
import com.kazimi.syarahweather.data.net.response.common.Wind


data class CurrentWeatherResponse(
    @SerializedName("coord") val coord: Coord,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("main") val main: Main,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)  {

   
    data class Main(
        @SerializedName("temp") val temp: Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int
    ) 

   
    data class Sys(
        @SerializedName("country") val country: String
    ) 
}