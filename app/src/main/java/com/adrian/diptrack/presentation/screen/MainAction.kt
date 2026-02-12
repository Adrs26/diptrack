package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.model.Coordinate

sealed interface MainAction {

    data object AnimateToUserLocation : MainAction

    data class GetCurrentGeofenceStatus(
        val isPermissionGranted: Boolean,
        val isMapLoaded: Boolean
    ) : MainAction

    data object RefreshGetBusStops : MainAction

    data object MarkBusStopNearbyDialogAsShown : MainAction

    data class StartTracking(
        val destinationName: String,
        val destinationLocation: Coordinate
    ) : MainAction

    data object StopTracking : MainAction
}