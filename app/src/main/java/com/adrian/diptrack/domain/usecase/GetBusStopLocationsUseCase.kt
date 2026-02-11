package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.model.BusStopResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository

class GetBusStopLocationsUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(filter: ((Coordinate) -> Boolean)? = null): BusStopResult {
        val filteredBusStop = if (filter != null) {
            val result = repository.getBusStopLocations()

            BusStopResult(
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