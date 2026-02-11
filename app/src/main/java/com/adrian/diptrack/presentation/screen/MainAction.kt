package com.adrian.diptrack.presentation.screen

import com.google.android.gms.maps.model.LatLng

sealed interface MainAction {

    data object AnimateToUserLocation : MainAction

    data class GetCurrentGeofenceStatus(
        val isPermissionGranted: Boolean,
        val isMapLoaded: Boolean
    ) : MainAction

    data object RefreshGetBusStops : MainAction

    data class StartTracking(
        val destinationName: String,
        val destinationLocation: LatLng
    ) : MainAction

    data object StopTracking : MainAction
}