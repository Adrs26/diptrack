package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Route
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.screen.util.MarkerUtil
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapsContent(
    cameraPositionState: CameraPositionState,
    busStops: List<BusStop>,
    routes: List<Route>,
    isLocationPermissionGranted: Boolean,
    isPolylinesShown: Boolean,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: () -> Unit,
) {
    val context = LocalContext.current

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
        busStops.forEach { busStop ->
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
            routes.forEach { route ->
                val polylinePoints = route.polylines.map { LatLng(it.latitude, it.longitude) }

                Polyline(
                    points = polylinePoints,
                    width = 12f,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}