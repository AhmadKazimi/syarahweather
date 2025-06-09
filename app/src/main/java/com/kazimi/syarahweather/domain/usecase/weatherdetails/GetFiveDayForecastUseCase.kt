package com.kazimi.syarahweather.domain.usecase.weatherdetails

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFiveDayForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    /**
     * Gets 5-day weather forecast with detailed information including:
     * - Min/Max temperatures
     * - Weather condition descriptions and icons
     * - Humidity and wind speed
     * - Date information
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return Flow<Result<WeatherData>> containing forecast data with detailed forecast list
     */
    operator fun invoke(latitude: Double, longitude: Double): Flow<Result<WeatherData>> {
        return weatherRepository.get5DaysForecastWeather(latitude, longitude)
            .map { weatherData ->
                // Filter and limit to 5 days of forecast data
                // The API might return more than 5 days, so we ensure we get exactly 5 days
                val filteredForecast = weatherData.forecast
                    .distinctBy { it.date.split(" ").firstOrNull() } // Group by date (remove time part)
                    .take(5) // Take only 5 days
                
                weatherData.copy(forecast = filteredForecast)
            }
            .toResult()
    }
} 