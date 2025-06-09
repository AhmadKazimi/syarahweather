package com.kazimi.syarahweather.domain.usecase.placesearch

import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import com.kazimi.syarahweather.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    operator fun invoke(query: String): Flow<Result<List<PlaceSearchResult>>> {
        return placesRepository.searchPlaces(query)
    }
} 