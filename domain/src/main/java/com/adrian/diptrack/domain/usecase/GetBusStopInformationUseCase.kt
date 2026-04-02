package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.manager.MapsPermissionResult
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusEtaInformation
import com.adrian.diptrack.domain.model.BusStopInformation
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.ceil

class GetBusStopInformationUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase
) {
    suspend operator fun invoke(busStopId: Int): GetBusStopInformationResult {
        val userLocation = when (val result = getCurrentUserLocationUseCase()) {
            is MapsPermissionResult.Denied -> return GetBusStopInformationResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        val busStop = repository.getBusStopById(busStopId)
        val busStopLocation = Coordinate(busStop.latitude, busStop.longitude)

        val distance = manager.calculateDistanceInKm(userLocation, busStopLocation)

        val flow = repository.getBusEtaForBusStop(busStopId).map { busEtas ->
            BusStopInformation(
                busStopName = busStop.name,
                distanceKilometers = distance,
                busEtas = busEtas.map { it.toBusEtaInformation() }
            )
        }

        return GetBusStopInformationResult.Success(flow)
    }

    private fun BusEta.toBusEtaInformation() = BusEtaInformation(
        busCode = busCode,
        numberPlate = numberPlate,
        etaMinutes = ceil(eta / 60.0).toInt()
    )
}

sealed interface GetBusStopInformationResult {
    data object LocationPermissionDenied : GetBusStopInformationResult
    data class  Success(val busStopInformation: Flow<BusStopInformation>) : GetBusStopInformationResult
}