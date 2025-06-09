package com.kazimi.syarahweather.ui.screens.placesearch

import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent

sealed interface PlacesSearchViewIntent : ViewIntent {
    data class SearchPlaces(val query: String) : PlacesSearchViewIntent
    data class AddLocation(val place: PlaceSearchResult) : PlacesSearchViewIntent
    object LoadSavedLocations : PlacesSearchViewIntent
    object ClearSearch : PlacesSearchViewIntent
}