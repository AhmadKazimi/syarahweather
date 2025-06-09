package com.kazimi.syarahweather.ui.screens.placesearch

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.kazimi.syarahweather.core.LocationConstants
import com.kazimi.syarahweather.core.base.viewmodel.ComposeBaseViewModel
import com.kazimi.syarahweather.core.base.viewmodel.ViewAction
import com.kazimi.syarahweather.core.base.viewmodel.ViewIntent
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.common.getOrNull
import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.usecase.managelocation.GetSavedLocationsUseCase
import com.kazimi.syarahweather.domain.usecase.managelocation.SaveNewLocationUseCase
import com.kazimi.syarahweather.domain.usecase.placesearch.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesSearchViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val saveNewLocationUseCase: SaveNewLocationUseCase,
    private val getSavedLocationsUseCase: GetSavedLocationsUseCase,
) : ComposeBaseViewModel() {

    private val _uiState = MutableStateFlow(PlacesSearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 300L
    }

    init {
        loadSavedLocations()
        setupSearchDebounce()
    }

    override suspend fun processViewIntent(intent: ViewIntent) {
        when (intent) {
            is PlacesSearchViewIntent.SearchPlaces -> handleSearchPlaces(intent.query)
            is PlacesSearchViewIntent.AddLocation -> handleAddLocation(intent.place)
            PlacesSearchViewIntent.LoadSavedLocations -> loadSavedLocations()
            PlacesSearchViewIntent.ClearSearch -> handleClearSearch()
        }
    }

    private fun setupSearchDebounce() = viewModelScope.launch {
            _searchQuery
                .debounce(SEARCH_DEBOUNCE_DELAY)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    performSearch(query)
                }
    }

    private fun handleSearchPlaces(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false,
                searchError = null
            )
        }
    }

    private fun performSearch(query: String) = viewModelScope.launch {
            searchPlacesUseCase(query).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isSearching = true,
                            searchError = null
                        )
                    }

                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = result.data,
                            isSearching = false,
                            searchError = null
                        )
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            searchError = result.appError.message
                        )
                        sendAction(PlacesSearchViewAction.ShowError(result.appError.message))
                    }
                }
            }
    }

    private fun handleAddLocation(place: PlaceSearchResult) = viewModelScope.launch {
        // Set loading state to true
        _uiState.value = _uiState.value.copy(
            isAddingLocation = true,
            addingLocationId = place.placeId
        )

        try {
            val savedLocation = place.toSavedLocation()
            saveNewLocationUseCase(savedLocation).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isAddingLocation = true,
                            addingLocationId = place.placeId
                        )
                    }

                    is Result.Success -> {
                        if (result.data) {
                            // Successfully saved, reload saved locations to get updated count
                            loadSavedLocations()
                            sendAction(PlacesSearchViewAction.ShowSuccess("${place.name} added successfully"))
                            // Navigate back to ManageLocationsScreen after successful addition
                            sendAction(PlacesSearchViewAction.NavigateBack)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isAddingLocation = false,
                                addingLocationId = null
                            )
                            sendAction(PlacesSearchViewAction.ShowError("Failed to save location"))
                        }
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isAddingLocation = false,
                            addingLocationId = null
                        )
                        sendAction(PlacesSearchViewAction.ShowError(result.appError.message))
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isAddingLocation = false,
                addingLocationId = null
            )
            sendAction(PlacesSearchViewAction.ShowError("Failed to save location: ${e.message}"))
        }
    }

    private fun loadSavedLocations() = viewModelScope.launch {
            try {
                val savedLocationsResult = getSavedLocationsUseCase().first()
                val savedLocations = savedLocationsResult.getOrNull() ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    savedLocations = savedLocations,
                    isMaxLocationsReached = savedLocations.size >= LocationConstants.MAX_SAVED_LOCATIONS,
                    isAddingLocation = false,
                    addingLocationId = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingLocation = false,
                    addingLocationId = null
                )
                sendAction(PlacesSearchViewAction.ShowError("Failed to load saved locations: ${e.message}"))
            }
    }

    private fun handleClearSearch() {
        _searchQuery.value = ""
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            isSearching = false,
            searchError = null
        )
    }
}

@Immutable
data class PlacesSearchUiState(
    val searchResults: List<PlaceSearchResult> = emptyList(),
    val savedLocations: List<SavedLocation> = emptyList(),
    val isSearching: Boolean = false,
    val isAddingLocation: Boolean = false,
    val addingLocationId: String? = null,
    val searchError: String? = null,
    val isMaxLocationsReached: Boolean = false,
    val isLocationServicesEnabled: Boolean = true
)


sealed interface PlacesSearchViewAction : ViewAction {
    data class ShowError(val message: String) : PlacesSearchViewAction
    data class ShowSuccess(val message: String) : PlacesSearchViewAction
    object NavigateBack : PlacesSearchViewAction
}