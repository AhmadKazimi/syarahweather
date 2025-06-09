package com.kazimi.syarahweather.ui.screens.weatherdetails

import android.location.Location
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.model.WeatherForecast
import com.kazimi.syarahweather.domain.usecase.managelocation.GetCurrentLocationUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.GetCurrentSavedLocationUseCase
import com.kazimi.syarahweather.domain.usecase.weatherdetails.GetWeatherUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.SetCurrentLocationUseCase
import com.kazimi.syarahweather.domain.usecase.weatherdetails.GetFiveDayForecastUseCase
import com.kazimi.syarahweather.core.base.viewmodel.ComposeBaseViewModel
import com.kazimi.syarahweather.core.base.viewmodel.ViewAction
import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent
import com.kazimi.syarahweather.ui.navigation.Screen
import com.kazimi.syarahweather.domain.datasource.device.PlatformLocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherDetailsViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getFiveDayForecastUseCase: GetFiveDayForecastUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getCurrentSavedLocationUseCase: GetCurrentSavedLocationUseCase,
    private val setCurrentLocationUseCase: SetCurrentLocationUseCase,
    private val platformLocationProvider: PlatformLocationProvider,
    savedStateHandle: SavedStateHandle
) : ComposeBaseViewModel() {

    private val _uiState = MutableStateFlow(WeatherDetailsUiState())
    val uiState = _uiState.asStateFlow()

    // Try to get location from navigation args, if none then use current location
    private val weatherDetailsArgs = try {
        savedStateHandle.toRoute<Screen.WeatherDetailsWithLocation>()
    } catch (e: Exception) {
        null
    }

    private var currentLocation: SavedLocation? = null

    init {
        if (weatherDetailsArgs != null) {
            // Use specific location from navigation
            currentLocation = SavedLocation(
                id = weatherDetailsArgs.locationId,
                name = weatherDetailsArgs.locationName,
                latitude = weatherDetailsArgs.latitude,
                longitude = weatherDetailsArgs.longitude,
                country = weatherDetailsArgs.country,
                state = weatherDetailsArgs.state
            )
            _uiState.update { it.copy(hasLocation = true) }
            refreshWeatherData()
        } else {
            // Use current location
            loadCurrentLocation()
        }
    }

    override suspend fun processViewIntent(intent: ViewIntent) {
        when (intent) {
            is WeatherDetailsViewIntent.LoadWeatherData -> loadWeatherData()
            is WeatherDetailsViewIntent.LoadFiveDayForecast -> loadFiveDayForecast()
            is WeatherDetailsViewIntent.RefreshWeatherData -> refreshWeatherData()
            is WeatherDetailsViewIntent.NavigateBack -> sendAction(WeatherDetailsViewAction.NavigateBack)
            is WeatherDetailsViewIntent.NavigateToManageLocations -> {
                // Mark that we shouldn't auto navigate anymore when user comes back
                _uiState.update { it.copy(shouldAutoNavigate = false) }
                sendAction(WeatherDetailsViewAction.NavigateToManageLocations(fromLocationIssue = false))
            }
        }
    }

    private suspend fun loadWeatherData() {
        currentLocation?.let { location ->
            getWeatherUseCase(location.latitude, location.longitude).map { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { 
                            it.copy(
                                isCurrentWeatherLoading = true, 
                                currentWeatherError = null
                            ) 
                        }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isCurrentWeatherLoading = false,
                                currentWeatherData = result.data,
                                currentWeatherError = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isCurrentWeatherLoading = false,
                                currentWeatherError = result.appError.message ?: "Failed to load current weather"
                            )
                        }
                    }
                }
            }.collect()
        }
    }

    private suspend fun loadFiveDayForecast() {
        currentLocation?.let { location ->
            getFiveDayForecastUseCase(location.latitude, location.longitude).map { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { 
                            it.copy(
                                isForecastLoading = true, 
                                forecastError = null
                            ) 
                        }
                    }

                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isForecastLoading = false,
                                forecastData = result.data.forecast,
                                forecastError = null
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isForecastLoading = false,
                                forecastError = result.appError.message ?: "Failed to load forecast data"
                            )
                        }
                    }
                }
            }.collect()
        }
    }

    private fun refreshWeatherData() = viewModelScope.launch {
        launch { loadWeatherData() }
        launch { loadFiveDayForecast() }
    }

    private fun loadCurrentLocation() = viewModelScope.launch {
        // First try to get saved current location
        getCurrentSavedLocationUseCase().collect { result ->
            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isCurrentWeatherLoading = true) }
                }
                is Result.Success -> {
                    val savedLocation = result.data
                    if (savedLocation != null) {
                        currentLocation = savedLocation
                        _uiState.update { 
                            it.copy(
                                hasLocation = true,
                                isCurrentWeatherLoading = false
                            ) 
                        }
                        refreshWeatherData()
                    } else {
                        // No saved current location, get device location  
                        _uiState.update { it.copy(isCurrentWeatherLoading = false) }
                        useCurrentDeviceLocation()
                    }
                }
                is Result.Error -> {
                    // Error getting saved location, reset loading and try device location
                    _uiState.update { it.copy(isCurrentWeatherLoading = false) }
                    useCurrentDeviceLocation()
                }
            }
        }
    }

    private fun useCurrentDeviceLocation() = viewModelScope.launch {
        if (!platformLocationProvider.hasLocationPermission()) {
            if (_uiState.value.shouldAutoNavigate) {
                // Auto navigate to manage locations when permission is not granted
                sendAction(WeatherDetailsViewAction.NavigateToManageLocations(fromLocationIssue = true))
            } else {
                // No permission - stop loading and show no location state
                _uiState.update { 
                    it.copy(
                        isCurrentWeatherLoading = false,
                        hasLocation = false
                    ) 
                }
            }
            return@launch
        }

        getCurrentLocationUseCase().collect { locationResult ->
            when (locationResult) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isCurrentWeatherLoading = true) }
                }
                is Result.Success -> {
                    locationResult.data?.let { location ->
                        val savedLocation = location
                        currentLocation = savedLocation
                        _uiState.update { it.copy(hasLocation = true) }
                        
                        // Save as current location
                        setCurrentLocationUseCase(savedLocation).collect { saveResult ->
                            when (saveResult) {
                                is Result.Success -> {
                                    refreshWeatherData()
                                }
                                is Result.Error -> {
                                    // Still proceed to show weather even if save fails
                                    refreshWeatherData()
                                }
                                is Result.Loading -> {
                                    // Continue loading
                                }
                            }
                        }
                    } ?: run {
                        if (_uiState.value.shouldAutoNavigate) {
                            // Auto navigate to manage locations when location is not available
                            sendAction(WeatherDetailsViewAction.NavigateToManageLocations(fromLocationIssue = true))
                        } else {
                            // No location available - stop loading and show no location state
                            _uiState.update { 
                                it.copy(
                                    isCurrentWeatherLoading = false,
                                    hasLocation = false
                                ) 
                            }
                        }
                    }
                }
                is Result.Error -> {
                    if (_uiState.value.shouldAutoNavigate) {
                        // Auto navigate to manage locations when location service fails
                        sendAction(WeatherDetailsViewAction.NavigateToManageLocations(fromLocationIssue = true))
                    } else {
                        // Location service failed - stop loading and show no location state
                        _uiState.update { 
                            it.copy(
                                isCurrentWeatherLoading = false,
                                hasLocation = false
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun createSavedLocationFromLocation(location: Location): SavedLocation {
        return SavedLocation(
            id = "current_${System.currentTimeMillis()}",
            name = "Current Location",
            latitude = location.latitude,
            longitude = location.longitude,
            country = null,
            state = null,
            isCurrentLocation = true
        )
    }


}

@Immutable
data class WeatherDetailsUiState(
    val isCurrentWeatherLoading: Boolean = false,
    val isForecastLoading: Boolean = false,
    val currentWeatherData: WeatherData? = null,
    val forecastData: List<WeatherForecast> = emptyList(),
    val currentWeatherError: String? = null,
    val forecastError: String? = null,
    val hasLocation: Boolean = false,
    val shouldAutoNavigate: Boolean = true
) {
    val hasCurrentWeatherError: Boolean get() = currentWeatherError != null
    val hasForecastError: Boolean get() = forecastError != null
    val showNoLocationState: Boolean get() = !hasLocation && !isCurrentWeatherLoading && !hasCurrentWeatherError

    val weatherData: WeatherData? get() = currentWeatherData?.copy(forecast = forecastData)
}

sealed interface WeatherDetailsViewAction : ViewAction {
    object NavigateBack : WeatherDetailsViewAction
    data class NavigateToManageLocations(val fromLocationIssue: Boolean = false) : WeatherDetailsViewAction
} 