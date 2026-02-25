package com.adrian.diptrack.presentation.screen

import com.google.android.gms.maps.CameraUpdate as MapsCameraUpdate

sealed class MainSideEffect {
    data class  CameraUpdate(val cameraUpdate: MapsCameraUpdate) : MainSideEffect()
    data object ShowToast : MainSideEffect()
}