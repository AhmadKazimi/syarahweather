package com.kazimi.syarahweather.ui.screens.managelocations.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazimi.syarahweather.R
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.ui.screens.managelocations.PermissionState

@Composable
fun CurrentLocationSection(
    permissionState: PermissionState,
    currentSavedLocation: SavedLocation?,
    currentLocationWeather: com.kazimi.syarahweather.domain.model.WeatherData?,
    onUpdateLocationPermissionClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {},
    onCurrentLocationClick: (SavedLocation) -> Unit = {}
) {
    when {
        // Permission granted - show current location with weather data
        permissionState.hasLocationPermission -> {
            Column {
                // Current location header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_current_location),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show current location item if available
                if (currentSavedLocation != null) {
                    if (currentLocationWeather != null) {
                        // Show actual weather data
                        LocationListItem(
                            country = currentLocationWeather.location?.name ?: "",
                            state = currentLocationWeather.location?.country ?: "",
                            temperature = currentLocationWeather.currentWeather?.temperature?.toString() ?: stringResource(R.string.general_loading),
                            weatherCondition = currentLocationWeather.currentWeather?.description ?: stringResource(R.string.general_loading),
                            onItemClick = { onCurrentLocationClick(currentSavedLocation) },
                            onDeleteClick = null, // Current location can't be deleted
                            isCurrentLocation = true
                        )
                    } else {
                        // Show loading state
                        LocationListItem(
                            country = stringResource(R.string.general_loading),
                            state = stringResource(R.string.general_loading),
                            temperature = "--°",
                            weatherCondition = stringResource(R.string.general_loading),
                            onItemClick = { },
                            onDeleteClick = null,
                            isCurrentLocation = true
                        )
                    }
                }
            }
        }

        // Permission permanently denied - show open settings button
        permissionState.isPermissionDeniedPermanently -> {
            Column {
                Text(
                    text = stringResource(R.string.permission_location_access_disabled),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.permission_enable_location_settings),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.action_open_settings),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Permission not granted - show permission request button
        else -> {
            Column {
                Text(
                    text = stringResource(R.string.location_get_weather_for_location),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.location_enable_access_instruction),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onUpdateLocationPermissionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.action_enable_location_access),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}