package com.adrian.diptrack.data.common.result

sealed interface CheckTrackingStatusResult {
    data object LocationPermissionDenied : CheckTrackingStatusResult
    data object BusFull : CheckTrackingStatusResult
    data object SwitchToIdle : CheckTrackingStatusResult
    data object SwitchToDrive : CheckTrackingStatusResult
    data object SwitchToArrive : CheckTrackingStatusResult
    data object Continue : CheckTrackingStatusResult
}