package com.adrian.diptrack.presentation.screen.handler

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.presentation.screen.MainSideEffect
import com.adrian.diptrack.presentation.screen.util.toMessageError
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.flow.Flow

@Composable
fun HandleSideEffect(
    sideEffect: Flow<MainSideEffect>,
    cameraPositionState: CameraPositionState,
    snackbarHostState: SnackbarHostState,
    error: NetworkError?,
    onRefreshFetchData: () -> Unit,
    onOpenBusStopInformationDialog: () -> Unit,
    onOpenBusInformationDialog: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is MainSideEffect.AnimateCamera -> {
                    cameraPositionState.animate(sideEffect.cameraUpdate, 1000)
                }
                is MainSideEffect.ShowLocationPermissionDeniedWarning -> {
                    Toast.makeText(
                        context,
                        "Mohon izinkan akses lokasi untuk menggunakan aplikasi",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is MainSideEffect.ShowOutsideBusStopWarning -> {
                    Toast.makeText(
                        context,
                        "Datang ke area halte bus untuk memulai pemantauan",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is MainSideEffect.ShowSameBusStopWarning -> {
                    Toast.makeText(
                        context,
                        "Pilih halte tujuan yang berbeda dari halte keberangkatan",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is MainSideEffect.ShowNoAvailableBusWarning -> {
                    Toast.makeText(
                        context,
                        "Tidak ada bus yang tersedia untuk tujuan yang dipilih",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is MainSideEffect.ShowBusStopInformationDialog -> onOpenBusStopInformationDialog()
                is MainSideEffect.ShowBusInformationDialog -> onOpenBusInformationDialog()
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            val result = snackbarHostState.showSnackbar(
                message = error.toMessageError().asString(context),
                actionLabel = "Refresh",
                withDismissAction = true
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onRefreshFetchData()
                SnackbarResult.Dismissed -> Unit
            }
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }
}