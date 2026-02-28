package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository

class StartTrackingUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase
) {
    suspend operator fun invoke(
        originLocation: Coordinate,
        destinationLocation: Coordinate
    ): StartTrackingResult {
        if (!isUserWithinBusStopRadius()) return StartTrackingResult.NotWithinBusStopRadius

        val isSameBusStop = originLocation.latitude.isCloseTo(destinationLocation.latitude) &&
                originLocation.longitude.isCloseTo(destinationLocation.longitude)

        if (isSameBusStop) return StartTrackingResult.SameBusStop

        val filteredStops =
            getFilteredBusStopsUseCase(originLocation, destinationLocation).filteredBusStops
        val distance = manager.calculateDistanceInKm(originLocation, destinationLocation)

        return StartTrackingResult.Success(filteredStops, distance)
    }

    private suspend fun isUserWithinBusStopRadius(): Boolean {
        val busStops = repository.getStaticData().busStops
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

    private fun Double.isCloseTo(other: Double, epsilon: Double = 1e-6): Boolean {
        return kotlin.math.abs(this - other) < epsilon
    }

    private companion object {
        const val GEOFENCE_RADIUS_METERS = 100f
    }
}

sealed interface StartTrackingResult {
    data object NotWithinBusStopRadius : StartTrackingResult
    data object SameBusStop : StartTrackingResult
    data class Success(
        val filteredStops: List<BusStop>,
        val distance: Double
    ) : StartTrackingResult
}