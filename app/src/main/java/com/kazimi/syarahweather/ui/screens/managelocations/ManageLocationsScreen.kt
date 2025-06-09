package com.kazimi.syarahweather.ui.screens.managelocations

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kazimi.syarahweather.R
import com.kazimi.syarahweather.ui.navigation.Screen
import com.kazimi.syarahweather.ui.screens.managelocations.component.CurrentLocationSection
import com.kazimi.syarahweather.ui.screens.managelocations.component.SavedLocationsSection
import com.kazimi.syarahweather.ui.util.PermissionExt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLocationsScreen(
    navController: NavController,
    fromLocationIssue: Boolean = false,
    viewModel: ManageLocationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.proceed(ManageLocationsViewIntent.LocationPermissionGranted)
        } else {
            val isPermanentlyDenied =
                PermissionExt.isLocationPermissionPermanentlyDenied(context as ComponentActivity)
            val shouldShowRationale = PermissionExt.shouldShowLocationPermissionRationale(context)

            if (isPermanentlyDenied) {
                showPermanentlyDeniedDialog = true
            }

            viewModel.proceed(
                ManageLocationsViewIntent.LocationPermissionDenied(
                    isPermanentlyDenied = isPermanentlyDenied,
                    shouldShowRationale = shouldShowRationale
                )
            )
        }
    }

    // Handle ViewActions from ViewModel
    LaunchedEffect(viewModel.viewAction) {
        viewModel.viewAction.collect {
            when (it) {
                is ManageLocationsViewAction.RequestPermission -> {
                    val shouldShowRationale =
                        PermissionExt.shouldShowLocationPermissionRationale(context as ComponentActivity)
                    if (shouldShowRationale) {
                        showPermissionRationaleDialog = true
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }

                is ManageLocationsViewAction.OpenAppSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }

                is ManageLocationsViewAction.OpenLocationSettings -> {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }

                is ManageLocationsViewAction.NavigateToWeatherDetails -> {
                    val location = it.location
                    navController.navigate(
                        Screen.WeatherDetailsWithLocation(
                            locationId = location.id,
                            locationName = location.name,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            country = location.country,
                            state = location.state
                        )
                    )
                    viewModel.resetViewAction()
                }

                is ManageLocationsViewAction.NavigateToPlacesSearch -> {
                    navController.navigate(Screen.PlacesSearch)
                    viewModel.resetViewAction()
                }

                is ManageLocationsViewAction.ShowMessage -> {
                    snackbarHostState.showSnackbar(it.message)
                }
            }
        }
    }

    // Check permission state when screen resumes (e.g., coming back from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.proceed(ManageLocationsViewIntent.CheckPermissionState)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                    // Only show back button if not coming from location issue
                    if (!fromLocationIssue) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.location_title_manage_locations),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            CurrentLocationSection(
                permissionState = uiState.permissionState,
                currentSavedLocation = uiState.currentSavedLocation,
                currentLocationWeather = uiState.currentLocationWeather,
                onUpdateLocationPermissionClick = {
                    viewModel.proceed(ManageLocationsViewIntent.RequestPermission)
                },
                onOpenSettingsClick = {
                    viewModel.proceed(ManageLocationsViewIntent.OpenAppSettings)
                },
                onCurrentLocationClick = { location ->
                    viewModel.proceed(ManageLocationsViewIntent.NavigateToWeatherDetails(location))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SavedLocationsSection(
                savedLocationsInfo = uiState.savedLocationList,
                isLoading = uiState.isLoadingSavedLocations,
                onLocationClick = { location ->
                    viewModel.proceed(ManageLocationsViewIntent.SelectLocation(location))
                },
                onDeleteLocation = { locationId ->
                    viewModel.proceed(ManageLocationsViewIntent.RemoveLocation(locationId))
                }
            )
        }

        // Floating Action Button - only show if max locations not reached
        if (!uiState.isMaxLocationsReached) {
            FloatingActionButton(
                onClick = {
                    viewModel.proceed(ManageLocationsViewIntent.NavigateToPlacesSearch)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.cd_search_locations),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Snackbar host for showing messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        LocationPermissionRationaleDialog(
            onAllowClick = {
                showPermissionRationaleDialog = false
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            onDenyClick = {
                showPermissionRationaleDialog = false
                viewModel.proceed(
                    ManageLocationsViewIntent.LocationPermissionDenied(
                        isPermanentlyDenied = false,
                        shouldShowRationale = true
                    )
                )
            }
        )
    }

    // Permanently Denied Dialog
    if (showPermanentlyDeniedDialog) {
        LocationPermissionPermanentlyDeniedDialog(
            onOpenSettingsClick = {
                showPermanentlyDeniedDialog = false
                viewModel.proceed(ManageLocationsViewIntent.OpenAppSettings)
            },
            onDismissClick = {
                showPermanentlyDeniedDialog = false
            }
        )
    }
}

@Composable
private fun LocationPermissionRationaleDialog(
    onAllowClick: () -> Unit,
    onDenyClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDenyClick,
        title = {
            Text(
                text = stringResource(R.string.permission_location_required_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_location_required_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onAllowClick) {
                Text(stringResource(R.string.action_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDenyClick) {
                Text(stringResource(R.string.action_not_now))
            }
        }
    )
}

@Composable
private fun LocationPermissionPermanentlyDeniedDialog(
    onOpenSettingsClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = {
            Text(
                text = stringResource(R.string.permission_location_disabled_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_location_disabled_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettingsClick) {
                Text(stringResource(R.string.action_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ManageLocationsScreenPreview() {
    ManageLocationsScreen(
        navController = NavController(LocalContext.current),
        fromLocationIssue = false
    )
}

