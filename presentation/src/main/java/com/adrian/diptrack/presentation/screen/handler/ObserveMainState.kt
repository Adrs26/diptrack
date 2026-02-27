package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import com.adrian.diptrack.domain.common.ModeState

@Composable
fun ObserveMainState(
    currentMode: ModeState,
    isLoading: Boolean,
    onShowBottomSheet: () -> Unit,
    onHideBottomSheet: () -> Unit,
    onShowLoadingDialog: () -> Unit,
    onHideLoadingDialog: () -> Unit
) {
    when (currentMode) {
        ModeState.Idle, ModeState.Arrive -> onHideBottomSheet()
        ModeState.Wait, ModeState.Drive -> onShowBottomSheet()
    }

    if (isLoading) onShowLoadingDialog() else onHideLoadingDialog()
}