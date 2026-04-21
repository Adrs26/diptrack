package com.adrian.diptrack.presentation.screen.handler

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.presentation.screen.MainSideEffect
import com.adrian.diptrack.presentation.service.TrackingService.Companion.NOTIFICATION_CHANNEL_ID
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.flow.Flow

@SuppressLint("LocalContextGetResourceValueCall", "MissingPermission")
@Composable
fun HandleSideEffect(
    sideEffect: Flow<MainSideEffect>,
    cameraPositionState: CameraPositionState,
    snackbarHostState: SnackbarHostState,
    error: NetworkError?,
    onRefreshFetchData: () -> Unit,
    onOpenBusStopDetailDialog: () -> Unit,
    onOpenBusDetailDialog: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is MainSideEffect.AnimateCamera -> {
                    cameraPositionState.animate(sideEffect.cameraUpdate, 500)
                }
                is MainSideEffect.ShowLocationPermissionDeniedWarning -> {
                    context.showToast("Izinkan akses lokasi untuk menggunakan fitur pemantauan")
                }
                is MainSideEffect.ShowChangeDestinationRejectedWarning -> {
                    context.showToast("Halte tujuan tidak bisa diubah selama di dalam bus")
                }
                is MainSideEffect.ShowOutsideBusStopWarning -> {
                    context.showToast("Datang ke area halte bus untuk memulai pemantauan")
                }
                is MainSideEffect.ShowSameBusStopWarning -> {
                    context.showToast("Pilih halte tujuan yang berbeda dari halte penjemputan")
                }
                is MainSideEffect.ShowNoAvailableBusWarning -> {
                    context.showToast("Tidak ada bus yang tersedia untuk halte ini")
                }
                is MainSideEffect.ShowBusFullWarning -> {
                    context.showToast("Bus penuh, rekomendasi telah diperbarui")
                }
                is MainSideEffect.ShowBusStopDetailDialog -> onOpenBusStopDetailDialog()
                is MainSideEffect.ShowBusDetailDialog -> onOpenBusDetailDialog()
                is MainSideEffect.ShowCancelTrackingNotification -> {
                    context.showNotification(
                        title = "Sesi Pemantauan Berakhir",
                        message = "Pemantauan dihentikan karena kamu tidak berada di dalam bus"
                    )
                }
                is MainSideEffect.ShowArrivalNotification -> {
                    context.showNotification(
                        title = "Perjalanan selesai",
                        message = "Bus telah tiba di halte tujuan. Sampai jumpa kembali!"
                    )
                }
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(error.toStringResource()),
                actionLabel = "Muat ulang",
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

private fun Context.showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

private fun NetworkError.toStringResource(): Int {
    return when(this) {
        NetworkErrorType.REQUEST_TIMEOUT -> R.string.request_timeout
        NetworkErrorType.UNAUTHORIZED -> R.string.unauthorized
        NetworkErrorType.CONFLICT -> R.string.conflict
        NetworkErrorType.NO_INTERNET -> R.string.no_internet
        NetworkErrorType.PAYLOAD_TOO_LARGE -> R.string.payload_too_large
        NetworkErrorType.SERVER_ERROR -> R.string.server_error
        NetworkErrorType.SERIALIZATION -> R.string.serialization_error
        else -> R.string.unknown_error
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun Context.showNotification(title: String, message: String) {
    val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_directions_bus)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(this).notify(2, notification)
}
