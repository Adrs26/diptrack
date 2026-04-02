package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.manager.MapsPermissionResult
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.flow.first
import kotlin.math.abs

class StartTrackingUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase
) {
    suspend operator fun invoke(destinationLocation: Coordinate): StartTrackingResult {
        val userLocation = when (val result = getCurrentUserLocationUseCase()) {
            is MapsPermissionResult.Denied -> return StartTrackingResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        if (!isUserWithinBusStopRadius(userLocation)) {
            return StartTrackingResult.NotWithinBusStopRadius
        }

        val originLocation = repository.getCurrentBusStopLocation().first()
        val isSameBusStop = originLocation.latitude.isCloseTo(destinationLocation.latitude) &&
                originLocation.longitude.isCloseTo(destinationLocation.longitude)

        if (isSameBusStop) return StartTrackingResult.SameBusStop

        val filteredStops =
            getFilteredBusStopsUseCase(originLocation, destinationLocation).filteredBusStops
        val distance = manager.calculateDistanceInKm(originLocation, destinationLocation)

        return StartTrackingResult.Success(filteredStops, distance)
    }

    private suspend fun isUserWithinBusStopRadius(userLocation: Coordinate): Boolean {
        val busStops = repository.getStaticData().busStops

        val userCoordinate = Coordinate(userLocation.latitude, userLocation.longitude)

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

    private fun Double.isCloseTo(other: Double) = abs(this - other) < EPSILON_CLOSE_RADIUS

    private companion object {
        const val GEOFENCE_RADIUS_METERS = 100f
        const val EPSILON_CLOSE_RADIUS = 1e-6
    }
}

sealed interface StartTrackingResult {
    data object LocationPermissionDenied : StartTrackingResult
    data object NotWithinBusStopRadius : StartTrackingResult
    data object SameBusStop : StartTrackingResult
    data class  Success(val filteredStops: List<BusStop>, val distance: Double) : StartTrackingResult
}