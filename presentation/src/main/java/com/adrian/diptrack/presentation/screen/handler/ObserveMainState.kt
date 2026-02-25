package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import com.adrian.diptrack.domain.common.ModeState

@Composable
fun ObserveMainState(
    currentMode: ModeState,
    isLoading: Boolean,
    onShowBottomSheet: () -> Unit,
    onShowLoadingDialog: () -> Unit,
    onDismissLoadingDialog: () -> Unit
) {
    when (currentMode) {
        ModeState.Idle -> Unit
        ModeState.Wait -> onShowBottomSheet()
        ModeState.Drive -> Unit
        ModeState.Arrive -> Unit
    }

    if (isLoading) onShowLoadingDialog() else onDismissLoadingDialog()
}