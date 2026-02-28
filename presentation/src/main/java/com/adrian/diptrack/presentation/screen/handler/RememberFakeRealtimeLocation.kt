package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.adrian.diptrack.presentation.screen.util.MarkerUtil.offsetLatLng
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay

@Composable
fun rememberFakeRealtimeLocation(
    start: LatLng
): State<LatLng> {

    val locationState = remember { mutableStateOf(start) }

    LaunchedEffect(Unit) {
        var current = start
        while (true) {
            delay(5000)

            current = offsetLatLng(
                from = current,
                distanceMeters = 10.0,
                bearingDegrees = 90.0
            )

            locationState.value = current
        }
    }

    return locationState
}