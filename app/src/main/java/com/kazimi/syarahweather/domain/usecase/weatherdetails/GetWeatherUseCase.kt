package com.kazimi.syarahweather.domain.usecase.weatherdetails

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(latitude: Double, longitude: Double): Flow<Result<WeatherData>> {
        return weatherRepository.getCurrentWeather(latitude, longitude).toResult()
    }
}