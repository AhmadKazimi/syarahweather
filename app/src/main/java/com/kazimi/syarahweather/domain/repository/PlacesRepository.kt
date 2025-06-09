package com.kazimi.syarahweather.domain.repository

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun searchPlaces(query: String): Flow<Result<List<PlaceSearchResult>>>
}