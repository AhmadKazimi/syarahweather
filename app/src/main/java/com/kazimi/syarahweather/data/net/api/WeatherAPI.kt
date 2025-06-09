package com.kazimi.syarahweather.data.net.api

import com.kazimi.syarahweather.data.net.response.CurrentWeatherResponse
import com.kazimi.syarahweather.data.net.response.ForecastWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Query("units") units: String? = "metric"
    ): Response<CurrentWeatherResponse>


    @GET("forecast")
    suspend fun get5DaysForecastWeather(
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Query("units") units: String? = "metric"
    ): Response<ForecastWeatherResponse>


} 