package com.adrian.diptrack.data.common.model

import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.entity.RouteEntity
import com.adrian.diptrack.data.remote.source.NetworkError

data class StaticData(
    val busStops: List<BusStopEntity>,
    val filteredBusStops: List<BusStopEntity>,
    val routes: List<RouteEntity>,
    val error: NetworkError?
)
