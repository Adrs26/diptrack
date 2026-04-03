package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.screen.BusState
import com.adrian.diptrack.presentation.screen.BusStopState

@Composable
fun DialogSection(
    dialogState: DialogState,
    currentMode: ModeState,
    busState: BusState,
    busStopState: BusStopState,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        DialogState.None -> Unit
        DialogState.Loading -> {
            LoadingDialog(
                text = stringResource(R.string.processing_data),
                modifier = Modifier.fillMaxWidth()
            )
        }
        DialogState.BusInformation -> {
            if (currentMode is ModeState.Wait || currentMode is ModeState.Drive) return
            BusInformationDialog(
                state = busState,
                onDismissRequest = onDismissRequest
            )
        }
        DialogState.BusStopInformation -> {
            if (currentMode is ModeState.Wait || currentMode is ModeState.Drive) return
            BusStopInformationDialog(
                state = busStopState,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

sealed class DialogState {
    data object None : DialogState()
    data object Loading : DialogState()
    data object BusInformation : DialogState()
    data object BusStopInformation : DialogState()
}