package com.adrian.diptrack.domain.model

data class StartTrackingSuccessData(
    val busId: Int,
    val busStops: List<BusStop>,
    val originBusStopId: Int,
    val originBusStopName: String,
    val destinationBusStopId: Int,
    val destinationBusStopName: String,
    val distance: Double
)
