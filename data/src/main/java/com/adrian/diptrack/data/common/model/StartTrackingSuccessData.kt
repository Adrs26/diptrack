package com.adrian.diptrack.data.common.model

import com.adrian.diptrack.data.local.entity.BusStopEntity

data class StartTrackingSuccessData(
    val busId: Int,
    val busStops: List<BusStopEntity>,
    val originBusStopId: Int,
    val originBusStopName: String,
    val destinationBusStopId: Int,
    val destinationBusStopName: String,
    val distance: Double
)
