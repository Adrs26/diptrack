package com.adrian.diptrack.presentation.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.component.BusStopPickersContent
import com.adrian.diptrack.presentation.screen.component.DialogSection
import com.adrian.diptrack.presentation.screen.component.DialogState
import com.adrian.diptrack.presentation.screen.component.MainContent
import com.adrian.diptrack.presentation.screen.handler.CollectCurrentLocationEvent
import com.adrian.diptrack.presentation.screen.handler.ObserveMainState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    state: MainState,
    cameraUpdateEvent: Flow<CameraUpdate>,
    onAction: (MainAction) -> Unit
) {
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isMapLoaded by rememberSaveable { mutableStateOf(false) }
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isPolylinesShown by rememberSaveable { mutableStateOf(false) }
    var isBusStopPickersVisible by rememberSaveable { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    ObserveMainState(
        showToast = state.showToast,
        currentMode = state.currentMode,
        onShowToast = {
            Toast.makeText(context, "Kamu tidak berada di area halte bus", Toast.LENGTH_LONG).show()
        },
        onShowBottomSheet = { isSheetVisible = true }
    )

    CollectCurrentLocationEvent(
        cameraUpdateEvent = cameraUpdateEvent,
        cameraPositionState = cameraPositionState
    )

    MainContent(
        mainState = state,
        etaMinutes = 5,
        distanceKilometers = 0.8,
        cameraPositionState = cameraPositionState,
        isLocationPermissionGranted = permissionState.status.isGranted,
        isMapLoaded = isMapLoaded,
        isSheetVisible = isSheetVisible,
        isPolylinesShown = isPolylinesShown,
        onMapLoaded = { isMapLoaded = true },
        onBusStopMarkerClick = { dialogState = DialogState.BusStopInformation },
        onMyLocationButtonClick = { onAction(MainAction.AnimateToUserLocation) },
        onPolylinesButtonClick = { isPolylinesShown = !isPolylinesShown },
        onMainButtonClick = { isBusStopPickersVisible = true },
        onProcessFetchData = { dialogState = DialogState.FetchDataLoading },
        onSucceedFetchData = { dialogState = DialogState.None },
        onRefreshFetchData = { onAction(MainAction.RefreshData) },
        onStopTrackingClick = {},
        onChangeDestinationClick = {}
    )

    AnimatedVisibility(
        visible = isBusStopPickersVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        BusStopPickersContent(
            busStops = if (state.initialDataResult is UiState.Success) {
                state.initialDataResult.data.busStops
            } else {
                emptyList()
            },
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

    BackHandler(isBusStopPickersVisible) {
        isBusStopPickersVisible = false
    }
}