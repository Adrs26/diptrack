package com.adrian.diptrack.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.DepartureBoard
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adrian.diptrack.presentation.screen.MainState
import com.google.maps.android.compose.CameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    mainState: MainState,
    etaMinutes: Int,
    distanceKilometers: Double,
    cameraPositionState: CameraPositionState,
    isLocationPermissionGranted: Boolean,
    isMapLoaded: Boolean,
    isSheetVisible: Boolean,
    onMapLoaded: () -> Unit,
    onBusStopMarkerClick: () -> Unit,
    onMyLocationButtonClick: () -> Unit,
    onMainButtonClick: () -> Unit,
    onRefreshGetBusStopLocations: () -> Unit,
    onStopTrackingClick: () -> Unit,
    onChangeDestinationClick: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }

    BottomSheetScaffold(
        sheetContent = {
            SheetContent(
                currentMode = mainState.currentMode,
                originName = mainState.originName,
                destinationName = mainState.destinationName,
                etaMinutes = etaMinutes,
                distanceKilometers = distanceKilometers,
                onStopTrackingClick = onStopTrackingClick,
                onChangeDestinationClick = onChangeDestinationClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = if (isSheetVisible) SheetValue.PartiallyExpanded else SheetValue.Hidden,
                skipHiddenState = isSheetVisible
            )
        ),
        sheetPeekHeight = 152.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShadowElevation = 8.dp,
        sheetDragHandle = { SheetHandle(modifier = Modifier.padding(vertical = 16.dp)) },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MapsContent(
                cameraPositionState = cameraPositionState,
                isLocationPermissionGranted = isLocationPermissionGranted,
                snackBarHostState = snackBarHostState,
                busStopLocations = mainState.busStopLocations,
                onMapLoaded = onMapLoaded,
                onBusStopMarkerClick = onBusStopMarkerClick,
                onRefreshGetBusStopLocations = onRefreshGetBusStopLocations
            )

            if (isMapLoaded) {
                FloatingActionButton(
                    onClick = onMyLocationButtonClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .size(36.dp),
                    shape = MaterialTheme.shapes.small,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                }
            }

            if (isMapLoaded && !isSheetVisible) {
                FloatingActionButton(
                    onClick = onMainButtonClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DepartureBoard,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp, 4.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}