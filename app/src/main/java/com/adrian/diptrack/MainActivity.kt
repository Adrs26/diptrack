package com.adrian.diptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import com.adrian.diptrack.presentation.screen.MainAction
import com.adrian.diptrack.presentation.screen.MainScreen
import com.adrian.diptrack.presentation.screen.MainViewModel
import com.adrian.diptrack.presentation.theme.DipTrackTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.orbitmvi.orbit.compose.collectAsState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DipTrackTheme {
                val state by viewModel.collectAsState()
                val sideEffect = viewModel.container.sideEffectFlow

                MainScreen(
                    state = state,
                    sideEffect = sideEffect,
                ) { action ->
                    when (action) {
                        is MainAction.AnimateToUserLocation -> {
                            viewModel.animateToUserLocation()
                        }
                        is MainAction.RefreshData -> {
                            viewModel.getInitialData()
                        }
                        is MainAction.StartTracking -> {
                            viewModel.startTracking(action.destinationName, action.destinationLocation)
                        }
                        is MainAction.CancelTracking -> {
                            viewModel.cancelTracking()
                        }
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