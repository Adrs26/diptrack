package com.adrian.diptrack.presentation.screen.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.R
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.presentation.screen.util.MarkerUtil
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapsContent(
    cameraPositionState: CameraPositionState,
    isLocationPermissionGranted: Boolean,
    busStops: List<BusStop>,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: () -> Unit
) {
    val context = LocalContext.current

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isTrafficEnabled = true,
            isMyLocationEnabled = isLocationPermissionGranted
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
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
    }
}