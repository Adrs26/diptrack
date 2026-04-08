package com.adrian.diptrack.domain.manager

import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate

interface MapsManager {

    suspend fun getCurrentUserLocation(): MapsPermissionResult

    fun calculateHaversine(startCoordinate: Coordinate, endCoordinate: Coordinate): Float

    fun calculateDistanceInKm(startCoordinate: Coordinate, endCoordinate: Coordinate): Double
}
