package com.kazimi.syarahweather.data.net.services

import com.kazimi.syarahweather.data.net.api.WeatherAPI
import retrofit2.Retrofit
import javax.inject.Inject

class WeatherService @Inject constructor(retrofit: Retrofit) {
    private val api: WeatherAPI by lazy { retrofit.create(WeatherAPI::class.java) }

    suspend fun getCurrentWeather(latitude: Double?, longitude: Double?) =
        api.getCurrentWeather(
            latitude = latitude,
            longitude = longitude
        )

    suspend fun get5DaysForecastWeather(latitude: Double?, longitude: Double?) =
        api.get5DaysForecastWeather(
            latitude = latitude,
            longitude = longitude
        )
}