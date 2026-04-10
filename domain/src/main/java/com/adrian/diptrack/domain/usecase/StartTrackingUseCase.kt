package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.common.result.StartTrackingResult
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StartTrackingSuccessData
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

class StartTrackingUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase
) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke(destinationLocation: Coordinate): StartTrackingResult {
        if (repository.currentMode.value is ModeState.Drive) {
            return StartTrackingResult.ChangeDestinationRejected
        }

        val userLocation = when (val result = getCurrentUserLocationUseCase()) {
            is MapsPermissionResult.Denied -> return StartTrackingResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        val originLocation =
            when (val result = handleBusStopResolution(userLocation, destinationLocation)) {
                null -> return StartTrackingResult.NotWithinBusStopRadius
                else -> result
            }

        val isSameBusStop = originLocation.latitude.isCloseTo(destinationLocation.latitude) &&
                originLocation.longitude.isCloseTo(destinationLocation.longitude)

        if (isSameBusStop) return StartTrackingResult.SameBusStop

        val currentBusStopId = repository.originBusStopId.value
        val destinationBusStopId = repository.destinationBusStopId.value

        val filteredIds = listOf(currentBusStopId, destinationBusStopId)
        val filteredStops = getFilteredBusStopsUseCase(filteredIds).filteredBusStops
        val distance = manager.calculateDistanceInKm(originLocation, destinationLocation)

        val busIds = withTimeoutOrNull(10.seconds) {
            repository.observeNearestAvailableBus(currentBusStopId)
                .debounce(1.seconds)
                .firstOrNull { it.isNotEmpty() }
        }

        return if (busIds == null) {
            StartTrackingResult.NoAvailableBus
        } else {
            repository.setCurrentMode(ModeState.Idle)
            delay(1.seconds)
            repository.setCurrentMode(ModeState.Wait(busIds[0]))
            repository.setCurrentBusId(busIds[0])
            StartTrackingResult.Success(
                StartTrackingSuccessData(
                    busId = busIds[0],
                    busStops = filteredStops,
                    originBusStopId = currentBusStopId,
                    originBusStopName =
                        filteredStops.firstOrNull { it.id == currentBusStopId }?.name.orEmpty(),
                    destinationBusStopId = destinationBusStopId,
                    destinationBusStopName =
                        filteredStops.firstOrNull { it.id == destinationBusStopId }?.name.orEmpty(),
                    distance = distance
                )
            )
        }
    }

    private suspend fun handleBusStopResolution(
        userLocation: Coordinate,
        destinationLocation: Coordinate
    ): Coordinate? {

        val busStops = repository.getStaticData().busStops

        val result = resolveBusStops(userLocation, destinationLocation, busStops) ?: return null

        val (origin, destination) = result
        val originLocation = Coordinate(origin.latitude, origin.longitude)

        repository.setOriginBusStopId(origin.id)
        repository.setOriginBusStopLocation(originLocation)
        repository.setDestinationBusStopId(destination.id)
        repository.setDestinationBusStopLocation(destinationLocation)

        return originLocation
    }

    private fun resolveBusStops(
        userLocation: Coordinate,
        destinationLocation: Coordinate,
        busStops: List<BusStop>
    ): BusStopSelection? {

        val userCoordinate = Coordinate(userLocation.latitude, userLocation.longitude)

        val originBusStop = busStops.firstOrNull { busStop ->
            val distance = manager.calculateHaversine(
                startCoordinate = userCoordinate,
                endCoordinate = Coordinate(busStop.latitude, busStop.longitude)
            )
            distance <= GEOFENCE_RADIUS_METERS
        }

        val destinationBusStop = busStops.firstOrNull {
            it.latitude.isCloseTo(destinationLocation.latitude) &&
                it.longitude.isCloseTo(destinationLocation.longitude)
        } ?: return null

        return originBusStop?.let { origin -> BusStopSelection(origin, destinationBusStop) }
    }

    private fun Double.isCloseTo(other: Double) = abs(this - other) < EPSILON_CLOSE_RADIUS

    private data class BusStopSelection(val origin: BusStop, val destination: BusStop)

    private companion object {
        private const val GEOFENCE_RADIUS_METERS = 100f
        private const val EPSILON_CLOSE_RADIUS = 1e-6
    }
}