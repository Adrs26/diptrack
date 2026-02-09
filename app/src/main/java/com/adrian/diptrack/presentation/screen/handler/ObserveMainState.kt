package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.presentation.screen.components.DialogState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ObserveMainState(
    currentMode: ModeState,
    currentGeofence: GeofenceState,
    onShowDialog: (DialogState) -> Unit
) {
    LaunchedEffect(currentMode, currentGeofence) {
        if (currentMode == ModeState.Idle && currentGeofence == GeofenceState.Enter) {
            delay(3.seconds)
            onShowDialog(DialogState.BusStopNearbyConfirmation)
        }
    }
}