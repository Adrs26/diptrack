package com.adrian.diptrack.domain.common

import com.adrian.diptrack.domain.model.BusStopLocation

data class BusStopLocationResult(
    val data: List<BusStopLocation>,
    val isOffline: Boolean,
    val error: NetworkError?
)