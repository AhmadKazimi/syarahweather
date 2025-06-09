package com.kazimi.syarahweather.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationHelper @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<Location?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        fun requestNewLocation() {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000L)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(10000L)
                .setMaxUpdateDelayMillis(30000L)
                .setMaxUpdates(1) // Only get one update
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        trySend(location)
                    } else {
                        trySend(null)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                    close()
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (!locationAvailability.isLocationAvailable) {
                        trySend(null)
                        fusedLocationClient.removeLocationUpdates(this)
                        close()
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        // Try to get last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                trySend(lastLocation)
                close()
                return@addOnSuccessListener
            }

            // If no last known location, request new location
            requestNewLocation()
        }.addOnFailureListener {
            // If getting last location fails, request new location
            requestNewLocation()
        }

        awaitClose()
    }

    fun hasLocationPermission(): Boolean {
        return PermissionExt.isLocationPermissionGranted(context)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}