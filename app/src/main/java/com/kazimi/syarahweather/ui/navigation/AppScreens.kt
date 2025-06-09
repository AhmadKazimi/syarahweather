package com.kazimi.syarahweather.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {

    @Serializable
    object Home : Screen

    @Serializable
    data class ManageLocations(
        val fromLocationIssue: Boolean = false
    ) : Screen

    @Serializable
    object WeatherDetails : Screen

    @Serializable
    data class WeatherDetailsWithLocation(
        val locationId: String,
        val locationName: String,
        val latitude: Double,
        val longitude: Double,
        val country: String? = null,
        val state: String? = null
    ) : Screen
    
    @Serializable
    object PlacesSearch : Screen
}