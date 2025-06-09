package com.kazimi.syarahweather.ui.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionExt {
    /**
     * Request permission if all permissions are not granted it will ask remaining
     *
     * Or return true if all permissions are granted
     */
    fun requestPermission(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int = 100,
    ): Boolean {
        val permissionsToRequest =
            permissions.filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
        if (permissionsToRequest.isEmpty()) return true

        ActivityCompat.requestPermissions(
            activity,
            permissionsToRequest.toTypedArray(),
            requestCode
        )
        return false
    }

    fun permissionIsGranted(
        context: Context,
        permissions: Array<String>,
    ): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    context,
                    it,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    fun shouldShowRational(
        activity: Activity,
        vararg permissions: String,
    ): Boolean {
        permissions.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                return true
            }
        }

        return false
    }

    /**
     * Location specific permission handling
     */
    fun getLocationPermissions(): Array<String> {
        return arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        return permissionIsGranted(context, getLocationPermissions())
    }

    fun requestLocationPermission(
        activity: Activity,
        requestCode: Int = 100
    ): Boolean {
        return requestPermission(activity, getLocationPermissions(), requestCode)
    }

    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        return shouldShowRational(activity, *getLocationPermissions())
    }

    fun isLocationPermissionPermanentlyDenied(activity: Activity): Boolean {
        val permissions = getLocationPermissions()
        return permissions.any { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
}
