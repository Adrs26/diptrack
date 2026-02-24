package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.model.Coordinate

sealed interface MainAction {

    data object AnimateToUserLocation : MainAction

    data object RefreshData : MainAction

    data class StartTracking(
        val destinationName: String,
        val destinationLocation: Coordinate
    ) : MainAction

    data object StopTracking : MainAction
}