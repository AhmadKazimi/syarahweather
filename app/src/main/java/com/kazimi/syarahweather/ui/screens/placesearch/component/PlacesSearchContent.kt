package com.kazimi.syarahweather.ui.screens.placesearch.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazimi.syarahweather.domain.model.PlaceSearchResult
import com.kazimi.syarahweather.ui.screens.placesearch.PlacesSearchUiState

@Composable
fun PlacesSearchContent(
    uiState: PlacesSearchUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddLocation: (PlaceSearchResult) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for a place...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current saved locations info
        if (uiState.savedLocations.isNotEmpty()) {
            Text(
                text = "Saved Locations (${uiState.savedLocations.size}/5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f, false)
            ) {
                items(uiState.savedLocations) { location ->
                    SavedLocationItem(
                        location = location,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Results or Status
        when {
            uiState.isSearching -> {
                SearchingIndicator()
            }
            searchQuery.isNotEmpty() && uiState.searchResults.isNotEmpty() -> {
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(uiState.searchResults) { place ->
                        val isAlreadySaved = uiState.savedLocations.any { savedLocation -> 
                            savedLocation.id == place.placeId || 
                            (savedLocation.latitude == place.latitude && savedLocation.longitude == place.longitude)
                        }
                        
                        PlaceSearchResultItem(
                            place = place,
                            canAddLocation = !uiState.isMaxLocationsReached && !isAlreadySaved,
                            isAddingLocation = uiState.addingLocationId == place.placeId,
                            onAddLocation = { onAddLocation(place) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            searchQuery.isNotEmpty() && uiState.searchResults.isEmpty() && !uiState.isSearching -> {
                EmptySearchResults()
            }
            else -> {
                SearchPrompt(hasLocations = uiState.savedLocations.isNotEmpty())
            }
        }
    }
} 