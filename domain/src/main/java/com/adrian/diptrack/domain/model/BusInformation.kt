package com.adrian.diptrack.domain.model

data class BusInformation(
    val busCode: String,
    val numberPlate: String,
    val driverName: String,
    val currPassenger: Int,
    val availPassenger: Int,
    val nextBusStopName: String
)
