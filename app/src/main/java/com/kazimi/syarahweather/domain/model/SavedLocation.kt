package com.kazimi.syarahweather.domain.model

data class SavedLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val state: String? = null,
    val isCurrentLocation: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
