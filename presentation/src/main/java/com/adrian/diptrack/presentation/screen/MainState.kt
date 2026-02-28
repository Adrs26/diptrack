package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Route

data class MainState(
    val currentMode: ModeState = ModeState.Idle,
    val busStops: List<BusStop> = emptyList(),
    val filteredBusStops: List<BusStop> = emptyList(),
    val routes: List<Route> = emptyList(),
    val distance: Double = 0.0,
    val originName: String = "",
    val destinationName: String = "",
    val isLoading: Boolean = false,
    val error: NetworkError? = null
)
