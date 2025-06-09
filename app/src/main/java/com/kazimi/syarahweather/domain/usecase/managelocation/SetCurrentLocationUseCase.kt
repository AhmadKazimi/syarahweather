package com.kazimi.syarahweather.domain.usecase.managelocation

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(location: SavedLocation):Flow<Result<Boolean>> {
        return locationRepository.setCurrentLocation(location).toResult()
    }

}