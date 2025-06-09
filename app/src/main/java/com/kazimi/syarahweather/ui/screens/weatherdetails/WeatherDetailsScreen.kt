package com.kazimi.syarahweather.ui.screens.weatherdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kazimi.syarahweather.domain.model.CurrentWeather
import com.kazimi.syarahweather.domain.model.WeatherForecast
import com.kazimi.syarahweather.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherDetailsScreen(
    navController: NavController,
    viewModel: WeatherDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle view actions
    LaunchedEffect(viewModel.viewAction) {
        viewModel.viewAction.collect { action ->
            when (action) {
                is WeatherDetailsViewAction.NavigateBack -> {
                    navController.popBackStack()
                    viewModel.resetViewAction()
                }
                is WeatherDetailsViewAction.NavigateToManageLocations -> {
                    navController.navigate(Screen.ManageLocations(fromLocationIssue = action.fromLocationIssue))
                    viewModel.resetViewAction()
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show back button only if we have navigation history
                        if (navController.previousBackStackEntry != null) {
                            IconButton(
                                onClick = { viewModel.proceed(WeatherDetailsViewIntent.NavigateBack) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            // Show locations button for root screen
                            IconButton(
                                onClick = { viewModel.proceed(WeatherDetailsViewIntent.NavigateToManageLocations) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Manage Locations",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .clickable { 
                                    viewModel.proceed(WeatherDetailsViewIntent.NavigateToManageLocations)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.weatherData?.location?.name ?: "Weather Details",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                uiState.weatherData?.location?.let { location ->
                                    val locationText = buildString {
                                        location.state?.let { append("$it, ") }
                                        location.country?.let { append(it) }
                                    }
                                    if (locationText.isNotBlank()) {
                                        Text(
                                            text = locationText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Location",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { viewModel.proceed(WeatherDetailsViewIntent.RefreshWeatherData) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                WeatherDetailsContent(
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToManageLocations = {
                        viewModel.proceed(WeatherDetailsViewIntent.NavigateToManageLocations)
                    }
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailsContent(
    uiState: WeatherDetailsUiState,
    modifier: Modifier = Modifier,
    onNavigateToManageLocations: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Weather Section
        when {
            uiState.isCurrentWeatherLoading -> {
                CurrentWeatherLoadingCard()
            }
            uiState.hasCurrentWeatherError -> {
                CurrentWeatherErrorCard(error = uiState.currentWeatherError!!)
            }
            uiState.currentWeatherData != null -> {
                CurrentWeatherCard(
                    currentWeather = uiState.currentWeatherData.currentWeather
                )
                
                WeatherDetailsCard(
                    currentWeather = uiState.currentWeatherData.currentWeather
                )
            }
            uiState.showNoLocationState -> {
                // No current location selected
                NoLocationCard(
                    onNavigateToManageLocations = onNavigateToManageLocations
                )
            }
        }
        
        // Forecast Section
        when {
            uiState.isForecastLoading -> {
                ForecastLoadingCard()
            }
            uiState.hasForecastError -> {
                ForecastErrorCard(error = uiState.forecastError!!)
            }
            uiState.forecastData.isNotEmpty() -> {
                ForecastCard(
                    forecast = uiState.forecastData.take(5)
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Loading current weather...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherErrorCard(
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ForecastLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading forecast...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastErrorCard(
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    currentWeather: CurrentWeather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentWeather.temperature.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = currentWeather.description,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Feels like ${currentWeather.feelsLike.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeatherDetailsCard(
    currentWeather: CurrentWeather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    label = "Humidity",
                    value = "${currentWeather.humidity}%"
                )
                WeatherDetailItem(
                    label = "Wind Speed",
                    value = "${currentWeather.windSpeed.roundToInt()} km/h"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    label = "Pressure",
                    value = "${currentWeather.pressure.roundToInt()} hPa"
                )
                WeatherDetailItem(
                    label = "Visibility",
                    value = "${currentWeather.visibility.roundToInt()} km"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    label = "UV Index",
                    value = currentWeather.uvIndex.roundToInt().toString()
                )
                WeatherDetailItem(
                    label = "Last Updated",
                    value = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(currentWeather.lastUpdated))
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ForecastCard(
    forecast: List<WeatherForecast>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(forecast) { forecastItem ->
                    ForecastItem(forecast = forecastItem)
                }
            }
        }
    }
}

@Composable
private fun ForecastItem(
    forecast: WeatherForecast,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Format date to show day of week
            val formattedDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE\nMM/dd", Locale.getDefault())
                val date = inputFormat.parse(forecast.date.split(" ").firstOrNull() ?: forecast.date)
                date?.let { outputFormat.format(it) } ?: forecast.date.take(5)
            } catch (e: Exception) {
                forecast.date.take(5)
            }
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weather icon based on icon code
            Box(
                modifier = Modifier
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getWeatherIcon(forecast.iconCode),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weather description
            Text(
                text = forecast.description.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Temperature range
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${forecast.maxTemp.roundToInt()}°",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${forecast.minTemp.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Additional details (humidity and wind)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "💧 ${forecast.humidity}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "💨 ${forecast.windSpeed.roundToInt()} km/h",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoLocationCard(
    onNavigateToManageLocations: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "No Location",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "No Location Selected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Please select a location to view weather details",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onNavigateToManageLocations,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Manage Locations",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Helper function to map icon codes to emoji icons
private fun getWeatherIcon(iconCode: String): String {
    return when (iconCode) {
        "01d", "01n" -> "☀️" // Clear sky
        "02d", "02n" -> "⛅" // Few clouds
        "03d", "03n" -> "☁️" // Scattered clouds
        "04d", "04n" -> "☁️" // Broken clouds
        "09d", "09n" -> "🌧️" // Shower rain
        "10d", "10n" -> "🌦️" // Rain
        "11d", "11n" -> "⛈️" // Thunderstorm
        "13d", "13n" -> "❄️" // Snow
        "50d", "50n" -> "🌫️" // Mist
        else -> "🌤️" // Default
    }
} 