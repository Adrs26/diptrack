package com.adrian.diptrack.data.local.database.projection

data class BusLocationProjection(
    val id: Int,
    val prevLatitude: Double,
    val prevLongitude: Double,
    val currLatitude: Double,
    val currLongitude: Double,
)
