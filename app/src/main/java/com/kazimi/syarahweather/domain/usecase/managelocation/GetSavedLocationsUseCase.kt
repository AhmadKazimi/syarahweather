package com.kazimi.syarahweather.domain.usecase.managelocation

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Result<List<SavedLocation>>> {
        return locationRepository.getAllSavedLocations().toResult()
    }
}