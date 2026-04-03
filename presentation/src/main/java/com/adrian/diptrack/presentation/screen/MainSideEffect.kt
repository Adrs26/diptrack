package com.adrian.diptrack.presentation.screen

import com.google.android.gms.maps.CameraUpdate

sealed class MainSideEffect {
    data class  AnimateCamera(val cameraUpdate: CameraUpdate) : MainSideEffect()
    data object ShowLocationPermissionDeniedWarning : MainSideEffect()
    data object ShowOutsideBusStopWarning : MainSideEffect()
    data object ShowSameBusStopWarning : MainSideEffect()
    data object ShowNoAvailableBusWarning : MainSideEffect()
    data object ShowBusStopInformationDialog : MainSideEffect()
    data object ShowBusInformationDialog : MainSideEffect()
}