package com.adrian.diptrack.domain.model

data class BusStopInformation(
    val busStopName: String,
    val distanceKilometers: Double,
    val busEtas: List<BusEtaInformation>
)

data class BusEtaInformation(
    val busCode: String,
    val numberPlate: String,
    val etaMinutes: Int
)
