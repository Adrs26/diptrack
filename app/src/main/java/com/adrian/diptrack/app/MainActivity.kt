package com.adrian.diptrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adrian.diptrack.presentation.screen.MainAction
import com.adrian.diptrack.presentation.screen.MainScreen
import com.adrian.diptrack.presentation.screen.MainViewModel
import com.adrian.diptrack.presentation.theme.DipTrackTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DipTrackTheme {
                val state by viewModel.mainState.collectAsStateWithLifecycle()
                val cameraUpdateEvent = viewModel.cameraUpdateEvent

                MainScreen(
                    state = state,
                    cameraUpdateEvent = cameraUpdateEvent,
                ) { action ->
                    when (action) {
                        is MainAction.AnimateToUserLocation -> {
                            viewModel.animateToUserLocation()
                        }
                        is MainAction.GetCurrentGeofenceStatus -> {
                            viewModel.getCurrentGeofenceStatus(
                                isGranted = action.isPermissionGranted,
                                isMapLoaded = action.isMapLoaded
                            )
                        }
                        is MainAction.RefreshData -> {
                            viewModel.getInitialData()
                            viewModel.getCurrentGeofenceStatus(
                                isGranted = true,
                                isMapLoaded = true
                            )
                        }
                        is MainAction.MarkBusStopNearbyDialogAsShown -> {
                            viewModel.markBusStopNearbyDialogAsShown()
                        }
                        is MainAction.StartTracking -> {}
                        is MainAction.StopTracking -> {}
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.animateToUserLocation()
    }
}