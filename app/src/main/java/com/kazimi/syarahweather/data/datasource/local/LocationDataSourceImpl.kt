package com.kazimi.syarahweather.data.datasource.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kazimi.syarahweather.core.di.IoDispatcher
import com.kazimi.syarahweather.data.datasource.converter.LocationJsonConverter
import com.kazimi.syarahweather.domain.datasource.local.LocationDataSource
import com.kazimi.syarahweather.domain.model.SavedLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val locationJsonConverter: LocationJsonConverter
) : LocationDataSource {
    private val savedLocationsKey = stringPreferencesKey("saved_locations")
    private val currentLocationKey = stringPreferencesKey("current_location")

    override fun getAllSavedLocations(): Flow<List<SavedLocation>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[savedLocationsKey] ?: ""
            locationJsonConverter.jsonToLocations(jsonString)
        }
    }

    override fun saveLocation(location: SavedLocation): Flow<Boolean> {
        return flow {
            runCatching {
                context.dataStore.edit { preferences ->
                    val currentList = getCurrentSavedLocations(preferences)
                    val updatedList = currentList.toMutableList()

                    // Remove existing location with same ID if exists
                    updatedList.removeAll { it.id == location.id }

                    // Add new location
                    updatedList.add(location)

                    preferences[savedLocationsKey] =
                        locationJsonConverter.locationsToJson(updatedList)
                }
                emit(true)
            }.onFailure {
                Log.e("LocationDataSource", "Error saving location", it)
                emit(false)
            }

        }.flowOn(coroutineDispatcher)
    }

    override fun removeLocation(locationId: String): Flow<Boolean> {
        return flow {
            runCatching {
                context.dataStore.edit { preferences ->
                    val currentList = getCurrentSavedLocations(preferences)
                    val updatedList = currentList.filterNot { it.id == locationId }
                    preferences[savedLocationsKey] =
                        locationJsonConverter.locationsToJson(updatedList)
                }
                emit(true)
            }.onFailure { emit(false) }
        }
    }


    override fun setCurrentLocation(location: SavedLocation): Flow<Boolean> {
        return flow {
            runCatching {
                context.dataStore.edit { preferences ->
                    preferences[currentLocationKey] = locationJsonConverter.locationToJson(location)
                }
                emit(true)
            }.onFailure {
                emit(false)
            }
        }
    }

    override fun getCurrentLocation(): Flow<SavedLocation?> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[currentLocationKey] ?: ""
            locationJsonConverter.jsonToLocation(jsonString)
        }
    }

    override fun clearAllLocations(): Flow<Boolean> {
        return flow {
            runCatching {
                context.dataStore.edit { preferences ->
                    preferences.remove(savedLocationsKey)
                    preferences.remove(currentLocationKey)
                }
                emit(true)
            }.onFailure { emit(false) }
        }
    }

    private fun getCurrentSavedLocations(preferences: Preferences): List<SavedLocation> {
        val jsonString = preferences[savedLocationsKey] ?: ""
        return locationJsonConverter.jsonToLocations(jsonString)
    }
}