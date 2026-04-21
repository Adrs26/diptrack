package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.model.Coordinate

sealed interface MainAction {
    data object OnAnimateToUserLocation : MainAction
    data object OnRefresh : MainAction
    data class  OnBusStopClicked(val busStopId: Int) : MainAction
    data class  OnBusClicked(val busId: Int) : MainAction
    data class  OnStartTracking(val destinationLocation: Coordinate) : MainAction
    data object OnCancelTracking : MainAction
}