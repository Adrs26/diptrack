package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.InitialDataResult
import com.adrian.diptrack.domain.repository.MainRepository

class GetFilteredBusStopsUseCase(
    private val manager: MapsManager,
    private val repository: MainRepository
) {
    suspend operator fun invoke(origin: Coordinate, destination: Coordinate): InitialDataResult {
        val initialData = repository.getInitialData()

        val filteredStops = initialData.busStops.filter { stop ->
            val stopCoordinate = Coordinate(stop.latitude, stop.longitude)

            manager.calculateHaversine(stopCoordinate, origin) <= LOCATION_THRESHOLD_METERS ||
            manager.calculateHaversine(stopCoordinate, destination) <= LOCATION_THRESHOLD_METERS
        }

        return initialData.copy(
            busStops = filteredStops
        )
    }

    private companion object {
        const val LOCATION_THRESHOLD_METERS = 5f
    }
}
