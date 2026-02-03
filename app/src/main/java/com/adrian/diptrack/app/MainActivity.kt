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
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DipTrackTheme {
                val viewModel = koinViewModel<MainViewModel>()
                val state by viewModel.mainState.collectAsStateWithLifecycle()
                val cameraUpdateEvent = viewModel.cameraUpdateEvent

                MainScreen(
                    state = state,
                    cameraUpdateEvent = cameraUpdateEvent,
                ) { action ->
                    when (action) {
                        is MainAction.MapReady -> {}
                        is MainAction.AnimateToUserLocation -> {
                            viewModel.animateToUserLocation()
                        }
                        is MainAction.RefreshGetBusStopLocations -> {
                            viewModel.getBusStopLocations()
                        }
                        is MainAction.StartTracking -> {}
                        is MainAction.StopTracking -> {}
                        is MainAction.Resume -> {}
                    }
                }
            }
        }
    }
}