package com.kazimi.syarahweather.ui.screens.managelocations

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.kazimi.syarahweather.core.LocationConstants
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.isSuccessThen
import com.kazimi.syarahweather.domain.common.error.AppError
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.usecase.managelocation.GetCurrentLocationUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.GetCurrentSavedLocationUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.GetSavedLocationsUseCase
import com.kazimi.syarahweather.domain.usecase.weatherdetails.GetWeatherUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.RemoveLocationUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.SetCurrentLocationUseCase
import com.kazimi.syarahweather.domain.usecase.permission.CheckLocationPermissionUseCase
import com.kazimi.syarahweather.core.base.viewmodel.ComposeBaseViewModel
import com.kazimi.syarahweather.core.base.viewmodel.ViewAction
import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageLocationsViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getCurrentSavedLocationUseCase: GetCurrentSavedLocationUseCase,
    private val getSavedLocationsUseCase: GetSavedLocationsUseCase,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val setCurrentLocationUseCase: SetCurrentLocationUseCase,
    private val removeLocationUseCase: RemoveLocationUseCase,
    private val checkLocationPermissionUseCase: CheckLocationPermissionUseCase,
) : ComposeBaseViewModel() {

    private val _uiState = MutableStateFlow(ManageLocationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    override suspend fun processViewIntent(intent: ViewIntent) {
        when (intent) {
            // Permission related intents
            is ManageLocationsViewIntent.CheckPermissionState -> checkPermissionState()
            is ManageLocationsViewIntent.LocationPermissionGranted -> handleLocationPermissionGranted()
            is ManageLocationsViewIntent.LocationPermissionDenied -> handleLocationPermissionDenied(
                isPermanentlyDenied = intent.isPermanentlyDenied,
                shouldShowRationale = intent.shouldShowRationale
            )
            is ManageLocationsViewIntent.RequestPermission -> sendAction(ManageLocationsViewAction.RequestPermission)
            is ManageLocationsViewIntent.OpenAppSettings -> sendAction(ManageLocationsViewAction.OpenAppSettings)
            is ManageLocationsViewIntent.OpenLocationSettings -> sendAction(ManageLocationsViewAction.OpenLocationSettings)

            // Location related intents
            is ManageLocationsViewIntent.LoadData -> loadInitialData()
            is ManageLocationsViewIntent.UseCurrentLocation -> useCurrentLocation()
            is ManageLocationsViewIntent.RefreshCurrentLocation -> refreshCurrentLocation()

            // Saved locations intents
            is ManageLocationsViewIntent.LoadSavedLocations -> loadSavedLocations()
            is ManageLocationsViewIntent.RemoveLocation -> removeLocation(intent.locationId)
            is ManageLocationsViewIntent.SelectLocation -> handleLocationSelection(intent.location)

            // Navigation intents
            is ManageLocationsViewIntent.NavigateToWeatherDetails -> sendAction(ManageLocationsViewAction.NavigateToWeatherDetails(intent.location))
            is ManageLocationsViewIntent.NavigateToPlacesSearch -> sendAction(ManageLocationsViewAction.NavigateToPlacesSearch)
        }
    }

    private fun loadInitialData() = viewModelScope.launch {
        // Check permissions first
        checkPermissionState()

        // Load saved locations
        loadSavedLocations()

        // Load current location if permission is granted
        if (checkLocationPermissionUseCase().hasLocationPermission) {
            loadCurrentLocation()
        }
    }

    private fun checkPermissionState() = viewModelScope.launch {
        val permissionInfo = checkLocationPermissionUseCase()

        _uiState.update { currentState ->
            currentState.copy(
                permissionState = currentState.permissionState.copy(
                    hasLocationPermission = permissionInfo.hasLocationPermission,
                    isLocationEnabled = permissionInfo.isLocationEnabled,
                    // Only reset permanently denied if permission is now granted
                    isPermissionDeniedPermanently = if (permissionInfo.hasLocationPermission) false else currentState.permissionState.isPermissionDeniedPermanently,
                    shouldShowRationale = false // Reset rationale flag when checking state
                )
            )
        }
    }

    private fun handleLocationPermissionGranted() = viewModelScope.launch {
        _uiState.update {
            it.copy(
                permissionState = it.permissionState.copy(
                    hasLocationPermission = true,
                    isPermissionDeniedPermanently = false
                )
            )
        }
        loadCurrentLocation()
    }

    private fun handleLocationPermissionDenied(isPermanentlyDenied: Boolean, shouldShowRationale: Boolean = false) = viewModelScope.launch {
        _uiState.update { currentState ->
            currentState.copy(
                permissionState = currentState.permissionState.copy(
                    hasLocationPermission = false,
                    isPermissionDeniedPermanently = isPermanentlyDenied,
                    shouldShowRationale = shouldShowRationale,
                    permissionRequestCount = currentState.permissionState.permissionRequestCount + 1
                )
            )
        }
    }

    private fun loadCurrentLocation() = viewModelScope.launch {
        // First try to get saved current location
        getCurrentSavedLocationUseCase().collect { result ->
            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingCurrentLocation = true) }
                }
                is Result.Success -> {
                    val savedLocation = result.data
                    if (savedLocation != null) {
                        _uiState.update {
                            it.copy(
                                currentSavedLocation = savedLocation,
                                isLoadingCurrentLocation = false
                            )
                        }
                        loadWeatherForCurrentLocation(savedLocation.latitude, savedLocation.longitude)
                    } else {
                        // No saved current location, get device location
                        useCurrentLocation()
                    }
                }
                is Result.Error -> {
                    // Error getting saved location, try device location
                    useCurrentLocation()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun useCurrentLocation() = viewModelScope.launch {
        getCurrentLocationUseCase()
            .onStart { 
                _uiState.update { it.copy(isLoadingCurrentLocation = true) }
            }
            .transformLatest { locationResult ->
                when (locationResult) {
                    is Result.Loading -> emit(locationResult)
                    is Result.Success -> {
                        locationResult.data?.let { location ->
                            val savedLocation = location
                            setCurrentLocationUseCase(savedLocation).collect { saveResult ->
                                emit(LocationSaveResult(saveResult, savedLocation, savedLocation))
                            }
                        } ?: emit(Result.Error(AppError(message = "Location data is null")))
                    }
                    is Result.Error -> emit(locationResult)
                }
            }
            .onEach { result ->
                handleLocationResult(result)
            }
            .catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoadingCurrentLocation = false,
                        error = AppError(message = "Failed to get location: ${exception.message}")
                    )
                }
            }
            .collect()
    }

    private fun handleLocationResult(result: Any) {
        when (result) {
            is Result.Loading -> {
                _uiState.update { it.copy(isLoadingCurrentLocation = true) }
            }
            is LocationSaveResult -> {
                when (result.saveResult) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                currentSavedLocation = result.savedLocation,
                                isLoadingCurrentLocation = false,
                                error = null
                            )
                        }
                        loadWeatherForCurrentLocation(result.location.latitude, result.location.longitude)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingCurrentLocation = false,
                                error = result.saveResult.appError
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Keep loading state - already handled above
                    }
                }
            }
            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingCurrentLocation = false,
                        error = result.appError
                    )
                }
            }
        }
    }

    private fun refreshCurrentLocation() = viewModelScope.launch {
        useCurrentLocation()
    }

    private fun loadSavedLocations() = viewModelScope.launch {
        getSavedLocationsUseCase().map { result ->
            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingSavedLocations = true) }
                }
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            savedLocationList = result.data,
                            isLoadingSavedLocations = false,
                            isMaxLocationsReached = result.data.size >= LocationConstants.MAX_SAVED_LOCATIONS
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingSavedLocations = false,
                            error = result.appError
                        )
                    }
                }
            }
        }.collect()
    }

    private fun removeLocation(locationId: String) = viewModelScope.launch {
        removeLocationUseCase(locationId).collect {
            it.isSuccessThen {
                sendAction(ManageLocationsViewAction.ShowMessage("Location removed"))
                loadSavedLocations()
            }
        }
    }

    private fun handleLocationSelection(location: SavedLocation) {
        sendAction(ManageLocationsViewAction.NavigateToWeatherDetails(location))
    }



    private fun loadWeatherForCurrentLocation(latitude: Double, longitude: Double) = viewModelScope.launch {
        getWeatherUseCase(latitude, longitude).collect { result ->
            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingWeather = true) }
                }
                is Result.Success -> {
                    val weather = result.data
                    _uiState.update {
                        it.copy(
                            currentLocationWeather = weather,
                            currentSavedLocation = it.currentSavedLocation?.copy(
                                country = weather.location.country,
                                state = weather.location.state,
                            ),
                            isLoadingWeather = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingWeather = false,
                            error = result.appError
                        )
                    }
                }
            }
        }
    }
}

@Immutable
data class ManageLocationsUiState(
    // Loading states
    val isLoadingCurrentLocation: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val isLoadingSavedLocations: Boolean = false,

    // Permission state
    val permissionState: PermissionState = PermissionState(),

    // Location data
    val currentSavedLocation: SavedLocation? = null,
    val currentLocationWeather: WeatherData? = null,
    val savedLocationList: List<SavedLocation> = emptyList(),

    // UI state flags
    val isMaxLocationsReached: Boolean = false,

    // Error state
    val error: AppError? = null
)

@Immutable
data class PermissionState(
    val hasLocationPermission: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val isPermissionDeniedPermanently: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val permissionRequestCount: Int = 0
)

// Helper data class for location save operations
private data class LocationSaveResult(
    val saveResult: Result<Boolean>,
    val savedLocation: SavedLocation,
    val location: SavedLocation
)

// View Actions for this screen
sealed interface ManageLocationsViewAction : ViewAction {
    object RequestPermission : ManageLocationsViewAction
    object OpenAppSettings : ManageLocationsViewAction
    object OpenLocationSettings : ManageLocationsViewAction
    data class NavigateToWeatherDetails(val location: SavedLocation) : ManageLocationsViewAction
    object NavigateToPlacesSearch : ManageLocationsViewAction
    data class ShowMessage(val message: String) : ManageLocationsViewAction
}
