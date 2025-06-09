package com.kazimi.syarahweather.domain.repository

import com.kazimi.syarahweather.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

interface DeviceLocationRepository {
    fun getDeviceLocation(): Flow<SavedLocation?>
}