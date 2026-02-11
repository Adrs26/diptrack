package com.adrian.diptrack.domain.model

import com.adrian.diptrack.domain.common.NetworkError

data class BusStopResult(
    val data: List<BusStop>,
    val isOffline: Boolean,
    val error: NetworkError?
)