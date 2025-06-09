package com.kazimi.syarahweather

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SyarahWeather : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_PLACES_API_KEY)
        }
    }
}