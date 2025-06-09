package com.kazimi.syarahweather.domain.usecase.managelocation

import com.kazimi.syarahweather.core.LocationConstants
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CanAddMoreLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Result<Boolean>> {
        return locationRepository.getAllSavedLocations()
            .map { locations -> locations.size < LocationConstants.MAX_SAVED_LOCATIONS }
            .toResult()
    }
} 