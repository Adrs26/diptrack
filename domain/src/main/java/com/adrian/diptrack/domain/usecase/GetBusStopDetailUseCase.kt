package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.result.GetBusStopDetailResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusStopDetail
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository

class GetBusStopDetailUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase
) {
    suspend operator fun invoke(busStopId: Int): GetBusStopDetailResult {
        val userLocation = when (val result = getCurrentUserLocationUseCase()) {
            is MapsPermissionResult.Denied -> return GetBusStopDetailResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        val busStop = repository.getBusStopById(busStopId)
        val busStopLocation = Coordinate(busStop.latitude, busStop.longitude)
        val busEtas = repository.getBusEtaForBusStop(busStopId)

        val distance = manager.calculateDistanceInKm(userLocation, busStopLocation)

        val busStopInformation = BusStopDetail(
            busStopName = busStop.name,
            distanceKilometers = distance,
            busEtas = busEtas
        )

        return GetBusStopDetailResult.Success(busStopInformation)
    }
}