package com.kazimi.syarahweather.ui.screens.placesearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kazimi.syarahweather.R
import com.kazimi.syarahweather.ui.screens.placesearch.component.PlacesSearchContent

@Composable
fun PlacesSearchScreen(
    navController: NavController,
    viewModel: PlacesSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle view actions
    LaunchedEffect(viewModel.viewAction) {
        viewModel.viewAction.collect { action ->
            when (action) {
                is PlacesSearchViewAction.ShowError -> {
                    snackbarHostState.showSnackbar(action.message)
                }

                is PlacesSearchViewAction.ShowSuccess -> {
                    snackbarHostState.showSnackbar(action.message)
                }

                PlacesSearchViewAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

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
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = stringResource(R.string.location_title_add_location),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PlacesSearchContent(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.proceed(PlacesSearchViewIntent.SearchPlaces(query))
                },
                onAddLocation = { place ->
                    viewModel.proceed(PlacesSearchViewIntent.AddLocation(place))
                },
                onClearSearch = {
                    viewModel.proceed(PlacesSearchViewIntent.ClearSearch)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Snackbar host for showing messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
} 