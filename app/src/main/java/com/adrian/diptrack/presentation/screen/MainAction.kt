package com.adrian.diptrack.presentation.screen

import com.google.android.gms.maps.model.LatLng

sealed interface MainAction {

    data class MapReady(
        val isPermissionGranted: Boolean,
        val isMapLoaded: Boolean
    ) : MainAction

    data object AnimateToUserLocation : MainAction

    data object RefreshGetBusStopLocations : MainAction

    data class StartTracking(
        val destinationName: String,
        val destinationLocation: LatLng
    ) : MainAction

    data object StopTracking : MainAction

    data class Resume(
        val isPermissionGranted: Boolean,
        val isMapLoaded: Boolean
    ) : MainAction
}