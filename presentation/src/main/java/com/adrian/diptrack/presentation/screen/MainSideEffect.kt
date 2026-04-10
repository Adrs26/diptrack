package com.adrian.diptrack.presentation.screen

import com.google.android.gms.maps.CameraUpdate

sealed class MainSideEffect {
    data class  AnimateCamera(val cameraUpdate: CameraUpdate) : MainSideEffect()
    data object ShowLocationPermissionDeniedWarning : MainSideEffect()
    data object ShowChangeDestinationRejectedWarning : MainSideEffect()
    data object ShowOutsideBusStopWarning : MainSideEffect()
    data object ShowSameBusStopWarning : MainSideEffect()
    data object ShowNoAvailableBusWarning : MainSideEffect()
    data object ShowBusFullWarning : MainSideEffect()
    data object ShowBusStopDetailDialog : MainSideEffect()
    data object ShowBusDetailDialog : MainSideEffect()
    data object ShowCancelTrackingNotification : MainSideEffect()
    data object ShowArrivalNotification : MainSideEffect()
}