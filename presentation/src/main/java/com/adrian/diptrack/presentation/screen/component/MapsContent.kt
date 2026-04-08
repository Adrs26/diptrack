package com.adrian.diptrack.presentation.screen.component

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Route
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.screen.util.MarkerUtil
import com.adrian.diptrack.presentation.screen.util.toLatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsContent(
    cameraPositionState: CameraPositionState,
    busStops: List<BusStop>,
    routes: List<Route>,
    busLocations: List<BusLocation>,
    isPolylinesShown: Boolean,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: (Int) -> Unit,
    onBusMarkerClick: (Int) -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = permissionState.status.isGranted,
            isTrafficEnabled = true
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        ),
        onMapLoaded = onMapLoaded
    ) {
        BusStopsLayer(
            busStops = busStops,
            onClick = onBusStopMarkerClick
        )
        RoutesLayer(
            routes = routes,
            isShown = isPolylinesShown
        )
        BusLayer(
            busLocations = busLocations,
            onClick = onBusMarkerClick
        )
    }
}

@Composable
private fun BusStopsLayer(
    busStops: List<BusStop>,
    onClick: (Int) -> Unit
) {
    val context = LocalContext.current

    val busStopIcon = remember { MarkerUtil.createBusStopMarker(context, R.drawable.ic_store) }

    busStops.forEach { busStop ->
        Marker(
            state = rememberUpdatedMarkerState(LatLng(busStop.latitude, busStop.longitude)),
            icon = busStopIcon,
            onClick = {
                onClick(busStop.id)
                true
            }
        )
    }
}

@Composable
private fun RoutesLayer(routes: List<Route>, isShown: Boolean) {
    if (!isShown) return

    routes.forEach { route ->
        Polyline(
            points = route.polylines.map { LatLng(it.latitude, it.longitude) },
            width = 12f,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BusLayer(
    busLocations: List<BusLocation>,
    onClick: (Int) -> Unit
) {
    val busIcon = remember { MarkerUtil.createBusMarker(R.drawable.ic_bus_top_view) }

    busLocations.forEach { busLocation ->
        val markerState = remember { MarkerState() }

        LaunchedEffect(busLocation.currentLocation) {
            val start = busLocation.previousLocation.toLatLng()
            val end = busLocation.currentLocation.toLatLng()

            val steps = 60
            val delayPerStep = 5000L / steps

            repeat(steps) { i ->
                val fraction = i / steps.toFloat()

                val lat = start.latitude + (end.latitude - start.latitude) * fraction
                val lng = start.longitude + (end.longitude - start.longitude) * fraction

                markerState.position = LatLng(lat, lng)

                delay(delayPerStep)
            }
        }

        val prevLatLng = remember(busLocation.previousLocation) {
            busLocation.previousLocation.toLatLng()
        }

        val currLatLng = remember(busLocation.currentLocation) {
            busLocation.currentLocation.toLatLng()
        }

        val bearing = remember(prevLatLng, currLatLng) {
            MarkerUtil.computeBearing(prevLatLng, currLatLng)
        }

        Marker(
            state = markerState,
            icon = busIcon,
            flat = true,
            rotation = bearing,
            onClick = {
                onClick(busLocation.id)
                true
            }
        )
    }
}