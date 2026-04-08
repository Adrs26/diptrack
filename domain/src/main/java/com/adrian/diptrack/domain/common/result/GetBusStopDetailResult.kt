package com.adrian.diptrack.domain.common.result

import com.adrian.diptrack.domain.model.BusStopDetail

sealed interface GetBusStopDetailResult {
    data object LocationPermissionDenied : GetBusStopDetailResult
    data class  Success(val busStopDetail: BusStopDetail) : GetBusStopDetailResult
}