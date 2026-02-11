package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.BusStopResult
import com.adrian.diptrack.presentation.common.UiState

data class MainState(
    val currentMode: ModeState = ModeState.Idle,
    val currentGeofence: GeofenceState = GeofenceState.Idle,
    val busStopResult: UiState<BusStopResult> = UiState.Empty,
    val originName: String = "",
    val destinationName: String = ""
)