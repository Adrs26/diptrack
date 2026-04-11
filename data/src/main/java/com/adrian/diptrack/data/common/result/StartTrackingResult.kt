package com.adrian.diptrack.data.common.result

import com.adrian.diptrack.data.common.model.StartTrackingSuccessData

sealed interface StartTrackingResult {
    data object LocationPermissionDenied : StartTrackingResult
    data object ChangeDestinationRejected : StartTrackingResult
    data object NotWithinBusStopRadius : StartTrackingResult
    data object SameBusStop : StartTrackingResult
    data object NoAvailableBus : StartTrackingResult
    data class  Success(val data: StartTrackingSuccessData) : StartTrackingResult
}