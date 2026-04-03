package com.adrian.diptrack.presentation.screen.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.adrian.diptrack.domain.common.ModeState

@Composable
fun HandleMainUiState(
    currentMode: ModeState,
    isLoading: Boolean,
    onShowBottomSheet: () -> Unit,
    onHideBottomSheet: () -> Unit,
    onShowLoadingDialog: () -> Unit,
    onHideLoadingDialog: () -> Unit
) {
    LaunchedEffect(currentMode) {
        when (currentMode) {
            is ModeState.Idle, ModeState.Arrive -> onHideBottomSheet()
            is ModeState.Wait -> onShowBottomSheet()
            is ModeState.Drive -> onShowBottomSheet()
        }
    }

    if (isLoading) onShowLoadingDialog() else onHideLoadingDialog()
}