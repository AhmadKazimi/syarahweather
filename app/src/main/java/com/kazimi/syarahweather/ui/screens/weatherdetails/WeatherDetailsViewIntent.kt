package com.kazimi.syarahweather.ui.screens.weatherdetails

import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent

sealed interface WeatherDetailsViewIntent : ViewIntent {
    object LoadWeatherData : WeatherDetailsViewIntent
    object LoadFiveDayForecast : WeatherDetailsViewIntent
    object RefreshWeatherData : WeatherDetailsViewIntent
    object NavigateBack : WeatherDetailsViewIntent
    object NavigateToManageLocations : WeatherDetailsViewIntent
} 