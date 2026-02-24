package com.adrian.diptrack.domain.manager

import com.adrian.diptrack.domain.model.Coordinate

interface MapsManager {

    suspend fun getCurrentUserLocation(): Coordinate

    fun calculateHaversine(startCoordinate: Coordinate, endCoordinate: Coordinate): Float
}
