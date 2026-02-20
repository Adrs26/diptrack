package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.InitialDataResult
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.component.DialogState

@Composable
fun ObserveMainState(
    currentMode: ModeState,
    currentGeofence: GeofenceState,
    initialDataResult: UiState<InitialDataResult>,
    hasShownBusStopNearbyDialog: Boolean,
    onShowDialog: (DialogState) -> Unit,
    onGetCurrentGeofenceStatus: () -> Unit
) {
    LaunchedEffect(currentGeofence, initialDataResult) {
        when (initialDataResult) {
            is UiState.Success -> {
                val hasNoError = initialDataResult.data.error == null
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