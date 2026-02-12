package com.adrian.diptrack.presentation.screen

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
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.screen.components.BusStopPickersContent
import com.adrian.diptrack.presentation.screen.components.DialogSection
import com.adrian.diptrack.presentation.screen.components.DialogState
import com.adrian.diptrack.presentation.screen.components.MainContent
import com.adrian.diptrack.presentation.screen.handler.CollectCurrentLocationEvent
import com.adrian.diptrack.presentation.screen.handler.ObserveMainState
import com.adrian.diptrack.presentation.screen.handler.handlePermissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
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
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isMapLoaded by rememberSaveable { mutableStateOf(false) }
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isBusStopPickersVisible by rememberSaveable { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    val permissionState = handlePermissions(
        isMapLoaded = isMapLoaded,
        onMapReady = { isPermissionGranted, isMapLoaded ->
            onAction(
                MainAction.GetCurrentGeofenceStatus(
                    isPermissionGranted = isPermissionGranted,
                    isMapLoaded = isMapLoaded
                )
            )
        }
    )

    ObserveMainState(
        currentMode = state.currentMode,
        currentGeofence = state.currentGeofence,
        busStopResult = state.busStopResult,
        hasShownBusStopNearbyDialog = state.hasShownBusStopNearbyDialog,
        onShowDialog = { dialogState = it },
        onGetCurrentGeofenceStatus = {
            onAction(
                MainAction.GetCurrentGeofenceStatus(
                    isPermissionGranted = permissionState.status.isGranted,
                    isMapLoaded = isMapLoaded
                )
            )
        }
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
        onMapLoaded = { isMapLoaded = true },
        onBusStopMarkerClick = { dialogState = DialogState.BusStopInformation },
        onMyLocationButtonClick = { onAction(MainAction.AnimateToUserLocation) },
        onMainButtonClick = { isBusStopPickersVisible = true },
        onProcessGetBusStops = { dialogState = DialogState.BusStopLoading },
        onSucceedGetBusStops = { dialogState = DialogState.None },
        onRefreshGetBusStops = { onAction(MainAction.RefreshGetBusStops) },
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
            busStops = if (state.busStopResult is UiState.Success) {
                state.busStopResult.data.data
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
        mainState = state,
        isMapLoaded = isMapLoaded,
        onShowBusStopList = { isBusStopPickersVisible = true },
        onDismissRequest = {
            dialogState = DialogState.None
            onAction(MainAction.MarkBusStopNearbyDialogAsShown)
        }
    )

    BackHandler(isBusStopPickersVisible) {
        isBusStopPickersVisible = false
    }
}