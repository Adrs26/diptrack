package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MapsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentGeofenceStatusUseCase(
    private val manager: MapsManager,
    private val repository: MapsRepository
) {
    suspend operator fun invoke() {
        val remoteBusStops = repository.getBusStopLocations().data
        val geofenceResult = getCurrentGeofenceStatus(remoteBusStops)

        if (geofenceResult != null) {
            repository.setGeofenceStatus(GeofenceState.Enter)
            repository.setCurrentBusStopId(geofenceResult.id)
            repository.setCurrentBusStopLocation(
                Coordinate(
                    latitude = geofenceResult.latitude,
                    longitude = geofenceResult.longitude
                )
            )
        } else {
            resetGeofenceState()
        }
    }

    private suspend fun resetGeofenceState() {
        repository.setGeofenceStatus(GeofenceState.Idle)
        repository.setCurrentBusStopId(-1)
        repository.setCurrentBusStopLocation(Coordinate(0.0, 0.0))
    }

    private suspend fun getCurrentGeofenceStatus(
        busStops: List<BusStop>
    ): BusStop? = withContext(Dispatchers.IO) {
        try {
            val location = manager.getLastKnownLocation() ?: return@withContext null

            val userCoordinate = Coordinate(
                location.latitude,
                location.longitude
            )

            busStops.firstOrNull { isInsideGeofence(userCoordinate, it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun isInsideGeofence(
        userCoordinate: Coordinate,
        busStop: BusStop
    ): Boolean {
        val distance = manager.calculateHaversine(
            startCoordinate = userCoordinate,
            endCoordinate = Coordinate(busStop.latitude, busStop.longitude)
        )
        return distance <= GEOFENCE_RADIUS_METER
    }

    companion object {
        private const val GEOFENCE_RADIUS_METER = 100f
    }
}
