package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.screen.BusArrivalInfoState

@Composable
fun SheetContent(
    currentMode: ModeState,
    state: BusArrivalInfoState,
    distanceKilometers: Double,
    originName: String,
    destinationName: String,
    onStopTrackingClick: () -> Unit,
    onChangeDestinationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SheetModeContent(
            etaMinutes = state.etaMinutes,
            currentMode = currentMode
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        if (currentMode is ModeState.Wait) {
            SheetBusContent(
                licensePlate = state.licensePlate,
                seatFilled = state.seatFilled,
                seatAvailable = state.seatAvailable,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp)
        ) {
            SheetRouteContent(
                currentMode = currentMode,
                originName = originName,
                destinationName = destinationName,
                distanceKilometers = distanceKilometers,
                onChangeDestinationClick = onChangeDestinationClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            if (currentMode is ModeState.Wait) {
                OutlinedButton(
                    onClick = onStopTrackingClick,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = stringResource(R.string.exit_waiting_mode),
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}