package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.BusStopLocationResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MapsRepository

class GetBusStopLocationsUseCase(
    private val repository: MapsRepository
) {
    suspend operator fun invoke(filter: ((Coordinate) -> Boolean)? = null): BusStopLocationResult {
        val filteredBusStop = if (filter != null) {
            val result = repository.getBusStopLocations()

            BusStopLocationResult(
                data = result.data.filter {
                    filter(
                        Coordinate(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    )
                },
                isOffline = result.isOffline,
                error = result.error
            )
        } else {
            repository.getBusStopLocations()
        }

        return filteredBusStop
    }
}