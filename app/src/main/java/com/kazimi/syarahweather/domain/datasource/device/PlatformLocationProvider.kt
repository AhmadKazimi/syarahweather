package com.kazimi.syarahweather.domain.datasource.device

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface PlatformLocationProvider {
    fun getCurrentLocation(): Flow<Location?>
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
} 