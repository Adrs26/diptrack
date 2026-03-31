package com.adrian.diptrack.presentation.screen.component

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
import androidx.compose.material.icons.filled.Route
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    state: MainState,
    etaMinutes: Int,
    cameraPositionState: CameraPositionState,
    snackbarHostState: SnackbarHostState,
    isSheetVisible: Boolean,
    onBusStopMarkerClick: () -> Unit,
    onMyLocationButtonClick: () -> Unit,
    onMainButtonClick: () -> Unit,
    onStopTrackingClick: () -> Unit,
    onChangeDestinationClick: () -> Unit
) {
    var isMapLoaded by rememberSaveable { mutableStateOf(false) }
    var isPolylinesShown by rememberSaveable { mutableStateOf(false) }

    BottomSheetScaffold(
        sheetContent = {
            SheetContent(
                currentMode = state.currentMode,
                originName = state.originName,
                destinationName = state.destinationName,
                etaMinutes = etaMinutes,
                distanceKilometers = state.distance,
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MapsContent(
                cameraPositionState = cameraPositionState,
                busStops = state.filteredBusStops,
                routes = state.routes,
                busLocations = state.busLocations,
                isPolylinesShown = isPolylinesShown,
                onMapLoaded = { isMapLoaded = true },
                onBusStopMarkerClick = onBusStopMarkerClick
            )

            if (isMapLoaded) {
                FloatingActionButton(
                    onClick = onMyLocationButtonClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .size(40.dp),
                    shape = MaterialTheme.shapes.small,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                }

                FloatingActionButton(
                    onClick = { isPolylinesShown = !isPolylinesShown },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 68.dp)
                        .size(40.dp),
                    shape = MaterialTheme.shapes.small,
                    containerColor = if (isPolylinesShown) MaterialTheme.colorScheme.primary else
                        Color.White,
                    contentColor = if (isPolylinesShown) MaterialTheme.colorScheme.onPrimary else
                        MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
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