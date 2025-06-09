package com.kazimi.syarahweather.domain.repository

import com.kazimi.syarahweather.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getCurrentWeather(latitude: Double, longitude: Double): Flow<WeatherData>
    fun get5DaysForecastWeather(latitude: Double, longitude: Double): Flow<WeatherData>
} 