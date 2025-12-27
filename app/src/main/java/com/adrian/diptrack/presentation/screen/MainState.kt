package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.BusStop

data class MainState(
    val currentMode: ModeState = ModeState.Wait,
    val currentGeofence: GeofenceState = GeofenceState.Idle,
    val busStops: List<BusStop> = emptyList(),
    val originName: String = "",
    val destinationName: String = ""
)