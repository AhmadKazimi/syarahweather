package com.kazimi.syarahweather.ui.screens.managelocations.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazimi.syarahweather.domain.model.SavedLocation

@Composable
fun SavedLocationsSection(
    savedLocationsInfo: List<SavedLocation>,
    isLoading: Boolean = false,
    onLocationClick: (SavedLocation) -> Unit,
    onDeleteLocation: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Saved locations section header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Saved locations (${savedLocationsInfo.size}/5)",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Saved locations list or empty state
        if (savedLocationsInfo.isEmpty() && !isLoading) {
            // Empty state placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved locations yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Search for a location to save it for quick access",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Saved locations list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedLocationsInfo.size) { index ->
                    val locationInfo = savedLocationsInfo[index]
                    LocationListItem(
                        country = locationInfo.country ?: "Unknown",
                        state = locationInfo.state ?: "Unknown",
                        onItemClick = { onLocationClick(locationInfo) },
                        onDeleteClick = { onDeleteLocation(locationInfo.id) }
                    )
                }
            }
        }
    }
}