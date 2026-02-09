package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.BusStopLocationResult
import com.adrian.diptrack.presentation.common.UiState

data class MainState(
    val currentMode: ModeState = ModeState.Idle,
    val currentGeofence: GeofenceState = GeofenceState.Idle,
    val busStopLocations: UiState<BusStopLocationResult> = UiState.Empty,
    val originName: String = "",
    val destinationName: String = ""
)