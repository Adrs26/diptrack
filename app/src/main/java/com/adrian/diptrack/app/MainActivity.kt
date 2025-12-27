package com.adrian.diptrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adrian.diptrack.presentation.screen.MainAction
import com.adrian.diptrack.presentation.screen.MainScreen
import com.adrian.diptrack.presentation.screen.MainState
import com.adrian.diptrack.presentation.theme.DipTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DipTrackTheme {
                MainScreen(
                    state = MainState(),
                ) { action ->
                    when (action) {
                        is MainAction.MapReady -> {}
                        is MainAction.AnimateToUserLocation -> {}
                        is MainAction.StartTracking -> {}
                        is MainAction.StopTracking -> {}
                        is MainAction.Resume -> {}
                    }
                }
            }
        }
    }
}