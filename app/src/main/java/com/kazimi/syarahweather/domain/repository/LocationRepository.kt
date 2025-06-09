package com.kazimi.syarahweather.domain.repository

import com.kazimi.syarahweather.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getAllSavedLocations(): Flow<List<SavedLocation>>
    fun getCurrentLocation(): Flow<SavedLocation?>
    fun saveLocation(location: SavedLocation): Flow<Boolean>
    fun removeLocation(locationId: String): Flow<Boolean>
    fun setCurrentLocation(location: SavedLocation): Flow<Boolean>
}