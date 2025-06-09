package com.kazimi.syarahweather.data.repository

import com.kazimi.syarahweather.core.di.IoDispatcher
import com.kazimi.syarahweather.domain.datasource.local.LocationDataSource
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LocationRepository {
    
    override fun getAllSavedLocations(): Flow<List<SavedLocation>> {
        return locationDataSource.getAllSavedLocations().flowOn(coroutineDispatcher)
    }

    override fun getCurrentLocation(): Flow<SavedLocation?> {
        return locationDataSource.getCurrentLocation().flowOn(coroutineDispatcher)
    }

    override fun saveLocation(location: SavedLocation): Flow<Boolean> {
        return locationDataSource.saveLocation(location).flowOn(coroutineDispatcher)
    }

    override fun removeLocation(locationId: String): Flow<Boolean> {
        return locationDataSource.removeLocation(locationId).flowOn(coroutineDispatcher)
    }

    override fun setCurrentLocation(location: SavedLocation): Flow<Boolean> {
        return locationDataSource.setCurrentLocation(location).flowOn(coroutineDispatcher)
    }
} 