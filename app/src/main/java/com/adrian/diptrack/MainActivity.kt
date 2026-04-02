package com.adrian.diptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import com.adrian.diptrack.presentation.screen.MainScreen
import com.adrian.diptrack.presentation.screen.MainViewModel
import com.adrian.diptrack.presentation.theme.DipTrackTheme
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DipTrackTheme {
                val viewModel = koinViewModel<MainViewModel>()
                val state by viewModel.collectAsState()
                val sideEffect = viewModel.container.sideEffectFlow

                MainScreen(
                    state = state,
                    sideEffect = sideEffect,
                    onAction = viewModel::onAction
                )
            }
        }
    }
}