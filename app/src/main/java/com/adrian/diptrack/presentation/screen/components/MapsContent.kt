package com.adrian.diptrack.presentation.screen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.R
import com.adrian.diptrack.domain.common.BusStopLocationResult
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.util.MarkerUtil
import com.adrian.diptrack.presentation.screen.util.toMessageError
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

@Composable
fun MapsContent(
    cameraPositionState: CameraPositionState,
    isLocationPermissionGranted: Boolean,
    snackBarHostState: SnackbarHostState,
    busStopLocations: UiState<BusStopLocationResult>,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: () -> Unit,
    onProcessGetBusStopLocations: () -> Unit,
    onSucceedGetBusStopLocations: () -> Unit,
    onRefreshGetBusStopLocations: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(busStopLocations) {
        if (busStopLocations is UiState.Success && busStopLocations.data.error != null) {
            scope.launch {
                val result = snackBarHostState.showSnackbar(
                    message = busStopLocations.data.error.toMessageError().asString(context),
                    actionLabel = "Refresh",
                    withDismissAction = true
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> onRefreshGetBusStopLocations()
                    SnackbarResult.Dismissed -> Unit
                }
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationPermissionGranted,
            isTrafficEnabled = true
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        ),
        onMapLoaded = onMapLoaded
    ) {
        when (busStopLocations) {
            is UiState.Success -> {
                busStopLocations.data.data.forEach { busStop ->
                    val markerState = rememberUpdatedMarkerState(
                        LatLng(busStop.latitude, busStop.longitude)
                    )

                    Marker(
                        state = markerState,
                        icon = MarkerUtil.createBusStopMarker(context, R.drawable.ic_store),
                        onClick = {
                            onBusStopMarkerClick()
                            true
                        }
                    )
                }
                onSucceedGetBusStopLocations()
            }
            is UiState.Loading -> onProcessGetBusStopLocations()
            else -> Unit
        }
    }
}