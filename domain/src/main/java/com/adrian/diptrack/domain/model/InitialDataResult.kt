package com.adrian.diptrack.domain.model

import com.adrian.diptrack.domain.common.NetworkError

data class InitialDataResult(
    val busStops: List<BusStop>,
    val routes: List<Route>,
    val isOffline: Boolean,
    val error: NetworkError?
)
