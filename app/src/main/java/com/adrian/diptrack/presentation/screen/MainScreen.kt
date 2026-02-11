package com.adrian.diptrack.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var isMapLoaded by rememberSaveable { mutableStateOf(false) }
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
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
        onMainButtonClick = { isSheetVisible = true },
        onProcessGetBusStops = { dialogState = DialogState.BusStopLoading },
        onSucceedGetBusStops = { dialogState = DialogState.None },
        onRefreshGetBusStops = { onAction(MainAction.RefreshGetBusStops) },
        onStopTrackingClick = {},
        onChangeDestinationClick = {}
    )

    DialogSection(
        dialogState = dialogState,
        mainState = state,
        isMapLoaded = isMapLoaded,
        onShowBusStopList = {  },
        onDismissRequest = { dialogState = DialogState.None }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onAction(
                    MainAction.GetCurrentGeofenceStatus(
                        isPermissionGranted = permissionState.status.isGranted,
                        isMapLoaded = isMapLoaded
                    )
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}