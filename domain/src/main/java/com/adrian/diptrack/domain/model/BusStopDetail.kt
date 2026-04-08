package com.adrian.diptrack.domain.model

data class BusStopDetail(
    val busStopName: String,
    val distanceKilometers: Double,
    val busEtas: List<BusEta>
)
