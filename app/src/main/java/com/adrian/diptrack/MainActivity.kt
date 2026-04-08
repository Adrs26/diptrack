package com.adrian.diptrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.adrian.diptrack.presentation.screen.MainAction
import com.adrian.diptrack.presentation.screen.MainScreen
import com.adrian.diptrack.presentation.screen.MainViewModel
import com.adrian.diptrack.presentation.service.TrackingService
import com.adrian.diptrack.presentation.theme.DipTrackTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.orbitmvi.orbit.compose.collectAsState

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = Intent(this, TrackingService::class.java)
        ContextCompat.startForegroundService(this, intent)

        setContent {
            DipTrackTheme {
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

    override fun onResume() {
        super.onResume()
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            viewModel.onAction(MainAction.OnAnimateToUserLocation)
        }
    }
}