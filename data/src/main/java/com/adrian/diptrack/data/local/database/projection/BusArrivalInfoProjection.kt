package com.adrian.diptrack.data.local.database.projection

data class BusArrivalInfoProjection(
    val busCode: String,
    val latitude: Double,
    val longitude: Double,
    val licensePlate: String,
    val maxCapacity: Int,
    val passenger: Int,
    val busStopName: String,
    val eta: Int
)
