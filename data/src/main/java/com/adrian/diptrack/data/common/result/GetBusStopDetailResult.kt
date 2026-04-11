package com.adrian.diptrack.data.common.result

import com.adrian.diptrack.data.common.model.BusStopDetail

sealed interface GetBusStopDetailResult {
    data object LocationPermissionDenied : GetBusStopDetailResult
    data class  Success(val busStopDetail: BusStopDetail) : GetBusStopDetailResult
}