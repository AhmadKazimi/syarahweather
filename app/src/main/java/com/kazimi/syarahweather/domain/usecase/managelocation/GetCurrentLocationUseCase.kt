package com.kazimi.syarahweather.domain.usecase.managelocation

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.toResult
import com.kazimi.syarahweather.domain.repository.DeviceLocationRepository
import com.kazimi.syarahweather.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val deviceLocationRepository: DeviceLocationRepository
) {
    operator fun invoke(): Flow<Result<SavedLocation?>> {
        return deviceLocationRepository.getDeviceLocation().toResult()
    }
}