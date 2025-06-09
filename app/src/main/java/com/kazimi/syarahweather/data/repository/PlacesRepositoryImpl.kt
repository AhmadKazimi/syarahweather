package com.kazimi.syarahweather.data.repository

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.kazimi.syarahweather.core.di.IoDispatcher
import com.kazimi.syarahweather.domain.common.error.AppError
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import com.kazimi.syarahweather.domain.repository.PlacesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class PlacesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlacesRepository {

    private val placesClient: PlacesClient by lazy {
        Places.createClient(context)
    }

    override fun searchPlaces(query: String): Flow<Result<List<PlaceSearchResult>>> = flow {
        emit(Result.Loading)
        
        try {
            if (query.trim().isEmpty()) {
                emit(Result.Success(emptyList()))
                return@flow
            }

            val predictions = getAutocompletePredictions(query)
            val places = fetchPlaceDetails(predictions)
            emit(Result.Success(places))
        } catch (e: Exception) {
            emit(Result.Error(AppError(message = e.message ?: "Failed to search places")))
        }
    }.flowOn(ioDispatcher)

    private suspend fun getAutocompletePredictions(query: String): List<AutocompletePrediction> {
        return suspendCancellableCoroutine { continuation ->
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    continuation.resume(response?.autocompletePredictions ?: emptyList())
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        continuation.resume(emptyList())
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }
        }
    }

    private suspend fun fetchPlaceDetails(predictions: List<AutocompletePrediction>): List<PlaceSearchResult> {
        val places = mutableListOf<PlaceSearchResult>()
        
        for (prediction in predictions) {
            try {
                val placeDetails = fetchSinglePlaceDetails(prediction.placeId)
                placeDetails?.let { places.add(it) }
            } catch (e: Exception) {
                // Continue with other places if one fails
                continue
            }
        }
        
        return places
    }

    private suspend fun fetchSinglePlaceDetails(placeId: String): PlaceSearchResult? {
        return suspendCancellableCoroutine { continuation ->
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )

            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            placesClient.fetchPlace(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val place = task.result?.place
                    if (place != null && place.location != null) {
                        val addressComponents = place.addressComponents?.asList()
                        
                        var country: String? = null
                        var state: String? = null
                        
                        addressComponents?.forEach { component ->
                            when {
                                component.types.contains("country") -> {
                                    country = component.name
                                }
                                component.types.contains("administrative_area_level_1") -> {
                                    state = component.name
                                }
                            }
                        }
                        
                        val result = PlaceSearchResult(
                            placeId = place.id ?: placeId,
                            name = place.displayName ?: "Unknown Place",
                            address = place.formattedAddress ?: "Unknown Address",
                            latitude = place.location!!.latitude,
                            longitude = place.location!!.longitude,
                            country = country,
                            state = state
                        )
                        continuation.resume(result)
                    } else {
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }
        }
    }
} 