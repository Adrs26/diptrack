package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.adrian.diptrack.presentation.R

@Composable
fun DialogSection(
    dialogState: DialogState,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        DialogState.None -> Unit
        DialogState.FetchDataLoading -> {
            LoadingDialog(
                text = stringResource(R.string.processing_data),
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
    }
}

sealed class DialogState {
    data object None : DialogState()
    data object FetchDataLoading : DialogState()
    data object BusInformation : DialogState()
    data object BusStopInformation : DialogState()
}