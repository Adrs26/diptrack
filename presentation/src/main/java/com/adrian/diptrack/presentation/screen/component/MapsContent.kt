package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.domain.model.InitialDataResult
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.util.MarkerUtil
import com.adrian.diptrack.presentation.screen.util.toMessageError
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

@Composable
fun MapsContent(
    initialDataResult: UiState<InitialDataResult>,
    cameraPositionState: CameraPositionState,
    snackBarHostState: SnackbarHostState,
    isLocationPermissionGranted: Boolean,
    isPolylinesShown: Boolean,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: () -> Unit,
    onProcessFetchData: () -> Unit,
    onSucceedFetchData: () -> Unit,
    onRefreshFetchData: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialDataResult) {
        if (initialDataResult is UiState.Success && initialDataResult.data.error != null) {
            scope.launch {
                val result = snackBarHostState.showSnackbar(
                    message = initialDataResult.data.error?.toMessageError()?.asString(context).orEmpty(),
                    actionLabel = "Refresh",
                    withDismissAction = true
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> onRefreshFetchData()
                    SnackbarResult.Dismissed -> Unit
                }
            }
        } else {
            snackBarHostState.currentSnackbarData?.dismiss()
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
        when (initialDataResult) {
            is UiState.Success -> {
                initialDataResult.data.busStops.forEach { busStop ->
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

                if (isPolylinesShown) {
                    initialDataResult.data.routes.forEach { route ->
                        val polylinePoints = route.polylines.map { LatLng(it.latitude, it.longitude) }

                        Polyline(
                            points = polylinePoints,
                            width = 12f,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                onSucceedFetchData()
            }
            is UiState.Loading -> onProcessFetchData()
            else -> Unit
        }
    }
}