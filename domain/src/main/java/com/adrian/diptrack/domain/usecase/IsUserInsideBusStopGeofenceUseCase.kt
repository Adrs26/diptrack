package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository

class IsUserInsideBusStopGeofenceUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository
) {
    suspend operator fun invoke(): Boolean {
        val busStops = repository.getInitialData().busStops
        val location = manager.getCurrentUserLocation()

        val userCoordinate = Coordinate(location.latitude, location.longitude)

        val matchedBusStop = busStops.firstOrNull { busStop ->
            val distance = manager.calculateHaversine(
                startCoordinate = userCoordinate,
                endCoordinate = Coordinate(busStop.latitude, busStop.longitude)
            )
            distance <= GEOFENCE_RADIUS_METERS
        }

        return if (matchedBusStop != null) {
            repository.setCurrentBusStopId(matchedBusStop.id)
            repository.setCurrentBusStopLocation(
                Coordinate(matchedBusStop.latitude, matchedBusStop.longitude)
            )
            true
        } else {
            false
        }
    }

    private companion object {
        const val GEOFENCE_RADIUS_METERS = 100f
    }
}
