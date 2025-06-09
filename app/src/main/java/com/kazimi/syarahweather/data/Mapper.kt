package com.kazimi.syarahweather.data

import com.kazimi.syarahweather.data.net.response.CurrentWeatherResponse
import com.kazimi.syarahweather.data.net.response.ForecastWeatherResponse
import com.kazimi.syarahweather.data.net.response.ForecastWeatherResponse.ForecastItem
import com.kazimi.syarahweather.domain.model.CurrentWeather
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.model.WeatherForecast

// Extension function to convert CurrentWeatherResponse to WeatherData
fun CurrentWeatherResponse.toWeatherData(): WeatherData {
    return WeatherData(
        location = SavedLocation(
            id = this.id.toString(),
            name = this.name,
            latitude = this.coord.lat,
            longitude = this.coord.lon,
            country = this.sys.country,
            state = null,
            isCurrentLocation = false
        ),
        currentWeather = CurrentWeather(
            temperature = this.main.temp,
            description = this.weather.firstOrNull()?.description ?: "",
            iconCode = this.weather.firstOrNull()?.icon ?: "",
            humidity = this.main.humidity,
            windSpeed = this.wind.speed,
            feelsLike = this.main.feelsLike,
            pressure = this.main.pressure.toDouble(),
            visibility = this.visibility.toDouble(),
            uvIndex = 0.0,
            lastUpdated = this.dt * 1000
        ),
        forecast = emptyList()
    )
}

// Extension function to convert ForecastWeatherResponse to WeatherData
fun ForecastWeatherResponse.toWeatherData(): WeatherData {

    return WeatherData(
        location = SavedLocation(
            id = this.city.id.toString(),
            name = this.city.name,
            latitude = this.city.coord.lat,
            longitude = this.city.coord.lon,
            country = this.city.country,
            state = null,
            isCurrentLocation = false
        ),
        currentWeather = this.list.firstOrNull()?.toCurrentWeather() ?: CurrentWeather(
            temperature = 0.0,
            description = "",
            iconCode = "",
            humidity = 0,
            windSpeed = 0.0,
            feelsLike = 0.0,
            pressure = 0.0,
            visibility = 0.0,
            uvIndex = 0.0
        ),
        forecast = this.list.map { it.toWeatherForecast() }
    )
}

// Helper extension function to convert ForecastItem to CurrentWeather
private fun ForecastItem.toCurrentWeather(): CurrentWeather {
    return CurrentWeather(
        temperature = this.main.temp,
        description = this.weather.firstOrNull()?.description ?: "",
        iconCode = this.weather.firstOrNull()?.icon ?: "",
        humidity = this.main.humidity,
        windSpeed = this.wind.speed,
        feelsLike = this.main.feelsLike,
        pressure = this.main.pressure.toDouble(),
        visibility = this.visibility.toDouble(),
        uvIndex = 0.0, // Not available in forecast API
        lastUpdated = this.dt * 1000 // Convert to milliseconds
    )
}

// Helper extension function to convert ForecastItem to WeatherForecast
private fun ForecastItem.toWeatherForecast(): WeatherForecast {
    return WeatherForecast(
        date = this.dtTxt,
        maxTemp = this.main.tempMax,
        minTemp = this.main.tempMin,
        description = this.weather.firstOrNull()?.description ?: "",
        iconCode = this.weather.firstOrNull()?.icon ?: "",
        humidity = this.main.humidity,
        windSpeed = this.wind.speed
    )
}

// Extension function to convert SavedLocation to coordinates for API calls
fun SavedLocation.toCoordinates(): Pair<Double, Double> {
    return Pair(this.latitude, this.longitude)
}