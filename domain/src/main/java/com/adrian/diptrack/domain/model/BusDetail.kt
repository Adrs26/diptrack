package com.adrian.diptrack.domain.model

data class BusDetail(
    val busCode: String,
    val licensePlate: String,
    val driver: String,
    val seatFilled: Int,
    val seatAvailable: Int,
    val nextBusStopName: String
)
