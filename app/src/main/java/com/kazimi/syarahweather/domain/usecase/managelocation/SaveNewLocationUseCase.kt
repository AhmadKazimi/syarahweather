package com.kazimi.syarahweather.domain.usecase.managelocation

import com.kazimi.syarahweather.core.LocationConstants
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.common.error.AppError
import com.kazimi.syarahweather.domain.common.error.ErrorType
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveNewLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(location: SavedLocation): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading)
            
            // First check current saved locations count
            val currentLocations = locationRepository.getAllSavedLocations().first()
            
            // Check if maximum locations reached
            if (currentLocations.size >= LocationConstants.MAX_SAVED_LOCATIONS) {
                emit(Result.Error(AppError(
                    type = ErrorType.LOCATION_MAX_EXCEEDED,
                    params = listOf(LocationConstants.MAX_SAVED_LOCATIONS)
                )))
                return@flow
            }
            
            // Check if location already exists
            val existingLocation = currentLocations.find { savedLocation ->
                savedLocation.id == location.id || 
                (savedLocation.latitude == location.latitude && savedLocation.longitude == location.longitude)
            }
            
            if (existingLocation != null) {
                emit(Result.Error(AppError(type = ErrorType.LOCATION_ALREADY_EXISTS)))
                return@flow
            }
            
            // Proceed with saving the location
            locationRepository.saveLocation(location).toResult().collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Result.Error(AppError(
                type = ErrorType.LOCATION_SAVE_FAILED,
                message = e.message ?: "",
                throwable = e
            )))
        }
    }
}