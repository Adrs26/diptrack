package com.adrian.diptrack.domain.model

data class BusLocation(
    val id: Int,
    val previousLocation: Coordinate,
    val currentLocation: Coordinate
)
