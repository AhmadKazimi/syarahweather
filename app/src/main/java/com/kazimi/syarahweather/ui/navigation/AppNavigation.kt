package com.kazimi.syarahweather.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kazimi.syarahweather.ui.screens.managelocations.ManageLocationsScreen
import com.kazimi.syarahweather.ui.screens.placesearch.PlacesSearchScreen
import com.kazimi.syarahweather.ui.screens.weatherdetails.WeatherDetailsScreen

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.padding(innerPadding),
        navController = navController, startDestination = Screen.WeatherDetails) {
        composable<Screen.WeatherDetails> {
            WeatherDetailsScreen(navController = navController)
        }
        
        composable<Screen.WeatherDetailsWithLocation> {
            WeatherDetailsScreen(navController = navController)
        }
        
        composable<Screen.ManageLocations> { backStackEntry ->
            val manageLocationsRoute = backStackEntry.toRoute<Screen.ManageLocations>()
            ManageLocationsScreen(
                navController = navController,
                fromLocationIssue = manageLocationsRoute.fromLocationIssue
            )
        }
        
        composable<Screen.PlacesSearch> {
            PlacesSearchScreen(navController = navController)
        }
    }
}
