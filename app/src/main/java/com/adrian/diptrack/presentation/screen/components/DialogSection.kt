package com.adrian.diptrack.presentation.screen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.adrian.diptrack.R
import com.adrian.diptrack.presentation.screen.MainState

@Composable
fun DialogSection(
    dialogState: DialogState,
    mainState: MainState,
    isMapLoaded: Boolean,
    onShowBusStopList: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        DialogState.None -> {}
        DialogState.BusStopLoading -> {
            LoadingDialog(
                text = stringResource(R.string.processing_bus_stop_data),
                modifier = Modifier.fillMaxWidth()
            )
        }
        DialogState.StartLoading -> {
            LoadingDialog(
                text = stringResource(R.string.processing_to_destination_data),
                modifier = Modifier.fillMaxWidth()
            )
        }
        DialogState.BusInformation -> {
            BusInformationDialog(
                onDismissRequest = onDismissRequest
            )
        }
        DialogState.BusStopInformation -> {
            BusStopInformationDialog(
                onDismissRequest = onDismissRequest
            )
        }
        DialogState.BusArriveToOriginInformation -> {
            InformationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.enter_driving_mode_confirmation,
                    mainState.originName
                ),
                onDismissRequest = onDismissRequest
            )
        }
        DialogState.BusArriveToDestinationInformation -> {
            InformationDialog(
                icon = Icons.Default.DirectionsBus,
                title = stringResource(R.string.bus_has_arrived),
                description = stringResource(
                    R.string.finish_trip_confirmation,
                    mainState.destinationName
                ),
                onDismissRequest = onDismissRequest
            )
        }
        DialogState.BusStopNearbyConfirmation -> {
            if (isMapLoaded) {
                ConfirmationDialog(
                    icon = Icons.Default.Store,
                    title = stringResource(R.string.bus_stop_detected),
                    description = stringResource(
                        R.string.enter_waiting_mode_confirmation,
                        mainState.originName
                    ),
                    onDismissRequest = onDismissRequest,
                    onConfirmRequest = {
                        onDismissRequest()
                        onShowBusStopList(true)
                    }
                )
            }
        }
    }
}

sealed class DialogState {
    data object None : DialogState()
    data object BusStopLoading : DialogState()
    data object StartLoading : DialogState()
    data object BusInformation : DialogState()
    data object BusStopInformation : DialogState()
    data object BusArriveToOriginInformation : DialogState()
    data object BusArriveToDestinationInformation : DialogState()
    data object BusStopNearbyConfirmation : DialogState()
}