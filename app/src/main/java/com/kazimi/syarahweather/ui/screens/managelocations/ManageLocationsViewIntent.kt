package com.kazimi.syarahweather.ui.screens.managelocations

import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent

sealed interface ManageLocationsViewIntent : ViewIntent {
    // Permission related intents
    object CheckPermissionState : ManageLocationsViewIntent
    object LocationPermissionGranted : ManageLocationsViewIntent
    data class LocationPermissionDenied(
        val isPermanentlyDenied: Boolean,
        val shouldShowRationale: Boolean = false
    ) : ManageLocationsViewIntent
    object RequestPermission : ManageLocationsViewIntent
    object OpenAppSettings : ManageLocationsViewIntent
    object OpenLocationSettings : ManageLocationsViewIntent

    // Data loading intents
    object LoadData : ManageLocationsViewIntent
    
    // Current location intents
    object UseCurrentLocation : ManageLocationsViewIntent
    object RefreshCurrentLocation : ManageLocationsViewIntent
    
    // Saved locations intents
    object LoadSavedLocations : ManageLocationsViewIntent
    data class RemoveLocation(val locationId: String) : ManageLocationsViewIntent
    data class SelectLocation(val location: SavedLocation) : ManageLocationsViewIntent

    // Navigation intents
    data class NavigateToWeatherDetails(val location: SavedLocation) : ManageLocationsViewIntent
    object NavigateToPlacesSearch : ManageLocationsViewIntent
}