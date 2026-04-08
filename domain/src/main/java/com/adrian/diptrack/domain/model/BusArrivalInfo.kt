package com.adrian.diptrack.domain.model

data class BusArrivalInfo(
    val busCode: String,
    val location: Coordinate,
    val licensePlate: String,
    val seatFilled: Int,
    val seatAvailable: Int,
    val busStopName: String,
    val etaMinutes: Int
)
