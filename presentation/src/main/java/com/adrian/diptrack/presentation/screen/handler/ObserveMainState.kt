package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.adrian.diptrack.domain.common.ModeState

@Composable
fun ObserveMainState(
    showToast: Boolean,
    currentMode: ModeState,
    onShowToast: () -> Unit,
    onShowBottomSheet: () -> Unit
) {
    LaunchedEffect(showToast) {
        if (showToast) {
            onShowToast()
        }
    }

    when (currentMode) {
        ModeState.Idle -> Unit
        ModeState.Wait -> onShowBottomSheet()
        ModeState.Drive -> Unit
        ModeState.Arrive -> Unit
    }
}