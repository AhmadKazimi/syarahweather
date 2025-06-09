package com.kazimi.syarahweather.data.net.response

import com.google.gson.annotations.SerializedName
import com.kazimi.syarahweather.data.net.response.common.Coord
import com.kazimi.syarahweather.data.net.response.common.Weather
import com.kazimi.syarahweather.data.net.response.common.Wind


data class ForecastWeatherResponse(
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: City
) {

    
    data class ForecastItem(
        @SerializedName("dt") val dt: Long,
        @SerializedName("main") val main: MainWeatherData,
        @SerializedName("weather") val weather: List<Weather>,
        @SerializedName("wind") val wind: Wind,
        @SerializedName("visibility") val visibility: Int,
        @SerializedName("dt_txt") val dtTxt: String
    )

    
    data class MainWeatherData(
        @SerializedName("temp") val temp: Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("temp_min") val tempMin: Double,
        @SerializedName("temp_max") val tempMax: Double,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int
    )

    
    data class City(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("coord") val coord: Coord,
        @SerializedName("country") val country: String
    )
}
