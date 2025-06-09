package com.kazimi.syarahweather.domain.model

data class PlaceSearchResult(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val state: String? = null
) {
    fun toSavedLocation(): SavedLocation {
        return SavedLocation(
            id = placeId,
            name = name,
            latitude = latitude,
            longitude = longitude,
            country = country,
            state = state,
            isCurrentLocation = false
        )
    }
} 