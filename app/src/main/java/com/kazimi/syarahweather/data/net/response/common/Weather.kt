package com.kazimi.syarahweather.data.net.response.common

import com.google.gson.annotations.SerializedName

/**
 * Common data classes shared between current and forecast weather responses
 */

data class Coord(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class Weather(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Wind(
    @SerializedName("speed") val speed: Double
) 