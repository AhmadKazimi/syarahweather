package com.kazimi.syarahweather.data.repository

import com.kazimi.syarahweather.core.di.IoDispatcher
import com.kazimi.syarahweather.domain.datasource.device.PlatformLocationProvider
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.repository.DeviceLocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceLocationRepositoryImpl @Inject constructor(
    private val platformLocationProvider: PlatformLocationProvider,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : DeviceLocationRepository {

    override fun getDeviceLocation(): Flow<SavedLocation?> {
        return platformLocationProvider.getCurrentLocation()
            .map { androidLocation ->
                androidLocation?.let { location ->
                    SavedLocation(
                        id = "current_${System.currentTimeMillis()}",
                        name = "Current Location",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        country = null,
                        state = null,
                        isCurrentLocation = true
                    )
                }
            }
            .flowOn(coroutineDispatcher)
    }
}