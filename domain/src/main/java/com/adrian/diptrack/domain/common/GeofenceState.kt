package com.adrian.diptrack.domain.common

sealed class GeofenceState {
    data object Idle : GeofenceState()
    data object Enter : GeofenceState()
    data object Exit : GeofenceState()
}
