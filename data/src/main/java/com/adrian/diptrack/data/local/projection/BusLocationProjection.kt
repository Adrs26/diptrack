package com.adrian.diptrack.data.local.projection

data class BusLocationProjection(
    val id: Int,
    val prevLatitude: Double,
    val prevLongitude: Double,
    val currLatitude: Double,
    val currLongitude: Double,
)
