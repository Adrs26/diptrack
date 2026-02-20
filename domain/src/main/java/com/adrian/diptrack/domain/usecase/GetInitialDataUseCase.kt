package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.InitialDataResult
import com.adrian.diptrack.domain.repository.MainRepository

class GetInitialDataUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(
        filterBusStop: ((Coordinate) -> Boolean)? = null
    ): InitialDataResult {
        val filteredBusStop = if (filterBusStop != null) {
            val result = repository.getInitialData()

            InitialDataResult(
                busStops = result.busStops.filter {
                    filterBusStop(
                        Coordinate(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    )
                },
                routes = result.routes,
                isOffline = result.isOffline,
                error = result.error
            )
        } else {
            repository.getInitialData()
        }

        return filteredBusStop
    }
}