package com.adrian.diptrack.domain.model

import com.adrian.diptrack.domain.common.NetworkError

data class StaticData(
    val busStops: List<BusStop>,
    val filteredBusStops: List<BusStop>,
    val routes: List<Route>,
    val error: NetworkError?
)
