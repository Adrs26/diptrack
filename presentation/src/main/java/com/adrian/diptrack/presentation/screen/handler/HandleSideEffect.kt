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
    onRefreshFetchData: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is MainSideEffect.CameraUpdate -> {
                    cameraPositionState.animate(sideEffect.cameraUpdate, 1000)
                }
                is MainSideEffect.ShowToast -> {
                    Toast.makeText(
                        context,
                        "Kamu tidak berada di area halte bus",
                        Toast.LENGTH_LONG
                    ).show()
                }
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