package com.kazimi.syarahweather.data.datasource.converter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kazimi.syarahweather.domain.model.SavedLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationJsonConverter @Inject constructor(
    private val gson: Gson
) {
    
    fun locationsToJson(locations: List<SavedLocation>): String {
        return gson.toJson(locations)
    }
    
    fun jsonToLocations(json: String): List<SavedLocation> {
        if (json.isBlank()) return emptyList()
        
        return try {
            val type = object : TypeToken<List<SavedLocation>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun locationToJson(location: SavedLocation): String {
        return gson.toJson(location)
    }
    
    fun jsonToLocation(json: String): SavedLocation? {
        if (json.isBlank()) return null
        
        return try {
            gson.fromJson(json, SavedLocation::class.java)
        } catch (e: Exception) {
            null
        }
    }
} 