package com.adrian.diptrack.data.manager

import android.location.Location
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapsManagerImpl(private val geolocator: Geolocator) : MapsManager {

    override suspend fun getCurrentUserLocation(): Coordinate {
        return when (val result = geolocator.current(Priority.HighAccuracy)) {
            is GeolocatorResult.Success -> {
                Coordinate(
                    result.data.coordinates.latitude,
                    result.data.coordinates.longitude
                )
            }
            is GeolocatorResult.Error -> Coordinate(0.0, 0.0)
        }
    }

    override fun calculateHaversine(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            startCoordinate.latitude,
            startCoordinate.longitude,
            endCoordinate.latitude,
            endCoordinate.longitude,
            results
        )
        return results[0]
    }

    override fun calculateDistanceInKm(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(endCoordinate.latitude - startCoordinate.latitude)
        val dLon = Math.toRadians(endCoordinate.longitude - startCoordinate.longitude)

        val rLat1 = Math.toRadians(startCoordinate.latitude)
        val rLat2 = Math.toRadians(endCoordinate.latitude)

        val a = sin(dLat / 2).pow(2) + cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
