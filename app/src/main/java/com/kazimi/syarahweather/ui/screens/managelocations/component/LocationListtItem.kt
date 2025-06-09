package com.kazimi.syarahweather.ui.screens.managelocations.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationListItem(
    country: String,
    state: String,
    onItemClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    temperature: String? = null,
    weatherCondition: String? = null,
    isCurrentLocation: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weather/Location icon
            Icon(
                imageVector = if (isCurrentLocation) Icons.Default.GpsFixed else Icons.Default.WbSunny,
                contentDescription = if (isCurrentLocation) "Current Location" else weatherCondition ?: state,
                tint = if (isCurrentLocation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Location and weather info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = country,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = state,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                )

                // Show weather condition if available
                if (weatherCondition != null && weatherCondition != "Loading...") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = weatherCondition,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Temperature display
            if (temperature != null && temperature != "Loading..." && temperature.isNotBlank()) {
                Text(
                    text = "${temperature}°C",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Delete button for saved locations (not for current location)
            if (onDeleteClick != null && !isCurrentLocation) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete location",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun LocationListItemPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column {
            LocationListItem(
                country = "Amman",
                state = "Amman, Jordan",
                temperature = "25",
                weatherCondition = "Sunny",
                onItemClick = {},
                onDeleteClick = {}
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LocationListItem(
                country = "Current Location",
                state = "New York, NY, USA",
                temperature = "22",
                weatherCondition = "Partly Cloudy",
                isCurrentLocation = true,
                onItemClick = {},
                onDeleteClick = null
            )
        }
    }
}
