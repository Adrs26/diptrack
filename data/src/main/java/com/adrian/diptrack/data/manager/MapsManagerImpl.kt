package com.adrian.diptrack.data.manager

import android.location.Location
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult

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
}
