package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.BusStopResult
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.components.DialogState

@Composable
fun ObserveMainState(
    currentMode: ModeState,
    currentGeofence: GeofenceState,
    busStopResult: UiState<BusStopResult>,
    hasShownBusStopNearbyDialog: Boolean,
    onShowDialog: (DialogState) -> Unit,
    onGetCurrentGeofenceStatus: () -> Unit
) {
    LaunchedEffect(currentGeofence, busStopResult) {
        when (busStopResult) {
            is UiState.Success -> {
                val hasNoError = busStopResult.data.error == null
                val isIdleMode = currentMode == ModeState.Idle
                val isEnteringGeofence = currentGeofence == GeofenceState.Enter
                val showBusStopNearbyDialog = hasNoError && isIdleMode && isEnteringGeofence

                if (showBusStopNearbyDialog && !hasShownBusStopNearbyDialog) {
                    onShowDialog(DialogState.BusStopNearbyConfirmation)
                }

                onGetCurrentGeofenceStatus()
            }
            else -> Unit
        }
    }
}