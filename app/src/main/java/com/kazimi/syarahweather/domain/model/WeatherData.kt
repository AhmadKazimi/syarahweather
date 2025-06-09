package com.kazimi.syarahweather.domain.model

data class WeatherData(
    val location: SavedLocation,
    val currentWeather: CurrentWeather,
    val forecast: List<WeatherForecast> = emptyList()
) 

data class CurrentWeather(
    val temperature: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Double,
    val lastUpdated: Long = System.currentTimeMillis()
) 

data class WeatherForecast(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double
) 