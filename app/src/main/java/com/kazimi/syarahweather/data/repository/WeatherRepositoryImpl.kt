package com.kazimi.syarahweather.data.repository

import com.kazimi.syarahweather.core.di.IoDispatcher
import com.kazimi.syarahweather.data.net.services.WeatherService
import com.kazimi.syarahweather.data.net.validate
import com.kazimi.syarahweather.data.toWeatherData
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    override fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Flow<WeatherData> {
        return flow {
            emit(
                weatherService.getCurrentWeather(latitude, longitude)
                    .validate().toWeatherData()
            )
        }.flowOn(coroutineDispatcher)
    }


    override fun get5DaysForecastWeather(
        latitude: Double,
        longitude: Double
    ): Flow<WeatherData> {
        return flow {
            emit(
                weatherService.get5DaysForecastWeather(latitude, longitude)
                    .validate().toWeatherData()
            )
        }.flowOn(coroutineDispatcher)
    }

} 