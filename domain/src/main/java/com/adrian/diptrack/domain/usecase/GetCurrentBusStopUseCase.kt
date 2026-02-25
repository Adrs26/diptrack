package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetCurrentBusStopUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(): Flow<Pair<String, Coordinate>> {
        val busStops = repository.getStaticData().busStops

        return combine(
            repository.getCurrentBusStopId(),
            repository.getCurrentBusStopLocation()
        ) { busStopId, busStopLocation ->
            val busStopName = busStops.firstOrNull { it.id == busStopId }?.name.orEmpty()

            Pair(busStopName, busStopLocation)
        }
    }
}