package com.kazimi.syarahweather.domain.usecase.permission

import com.kazimi.syarahweather.ui.util.LocationHelper
import javax.inject.Inject

class CheckLocationPermissionUseCase @Inject constructor(
    private val locationHelper: LocationHelper
) {
    operator fun invoke(): LocationPermissionInfo {
        return LocationPermissionInfo(
            hasLocationPermission = locationHelper.hasLocationPermission(),
            isLocationEnabled = locationHelper.isLocationEnabled()
        )
    }
}

data class LocationPermissionInfo(
    val hasLocationPermission: Boolean,
    val isLocationEnabled: Boolean
) 