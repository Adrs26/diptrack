package com.adrian.diptrack.presentation.screen.handler

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissions(
    onLocationPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = { isGranted ->
                if (!isGranted) {
                    context.showToast("Izinkan notifikasi untuk info perjalanan di latar belakang")
                }
            }
        )
    } else null

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResult = { isGranted ->
            onLocationPermissionGranted()
            if (isGranted) {
                notificationPermissionState?.launchPermissionRequest()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            onLocationPermissionGranted()
        }
    }
}

private fun Context.showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()