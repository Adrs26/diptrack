package com.adrian.diptrack.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adrian.diptrack.presentation.screen.component.BusStopPickersContent
import com.adrian.diptrack.presentation.screen.component.DialogSection
import com.adrian.diptrack.presentation.screen.component.DialogState
import com.adrian.diptrack.presentation.screen.component.MainContent
import com.adrian.diptrack.presentation.screen.handler.HandleMainUiState
import com.adrian.diptrack.presentation.screen.handler.HandleSideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    state: MainState,
    sideEffect: Flow<MainSideEffect>,
    onAction: (MainAction) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isBusStopPickersVisible by rememberSaveable { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    val snackbarHostState = remember { SnackbarHostState() }

    HandleMainUiState(
        currentMode = state.currentMode,
        isLoading = state.isLoading,
        onShowBottomSheet = { isSheetVisible = true },
        onHideBottomSheet = { isSheetVisible = false },
        onShowLoadingDialog = { dialogState = DialogState.Loading },
        onHideLoadingDialog = { dialogState = DialogState.None }
    )

    HandleSideEffect(
        sideEffect = sideEffect,
        cameraPositionState = cameraPositionState,
        snackbarHostState = snackbarHostState,
        error = state.error,
        onRefreshFetchData = { onAction(MainAction.RefreshData) }
    )

    MainContent(
        state = state,
        etaMinutes = 5,
        cameraPositionState = cameraPositionState,
        snackbarHostState = snackbarHostState,
        isSheetVisible = isSheetVisible,
        onBusStopMarkerClick = { dialogState = DialogState.BusStopInformation },
        onMyLocationButtonClick = { onAction(MainAction.AnimateToUserLocation) },
        onMainButtonClick = { isBusStopPickersVisible = true },
        onStopTrackingClick = { onAction(MainAction.CancelTracking) },
        onChangeDestinationClick = { isBusStopPickersVisible = true }
    )

    AnimatedVisibility(
        visible = isBusStopPickersVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        BusStopPickersContent(
            busStops = state.busStops,
            onNavigateBack = { isBusStopPickersVisible = false },
            onBusStopSelect = { destinationName, destinationLocation ->
                onAction(MainAction.StartTracking(destinationName, destinationLocation))
            }
        )
    }

    DialogSection(
        dialogState = dialogState,
        onDismissRequest = { dialogState = DialogState.None }
    )

    BackHandler(isBusStopPickersVisible) { isBusStopPickersVisible = false }
}