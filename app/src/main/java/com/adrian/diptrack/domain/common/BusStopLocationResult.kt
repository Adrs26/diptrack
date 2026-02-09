package com.adrian.diptrack.domain.common

import com.adrian.diptrack.domain.model.BusStop

data class BusStopLocationResult(
    val data: List<BusStop>,
    val isOffline: Boolean,
    val error: NetworkError?
)