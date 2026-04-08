package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository

class GetFilteredBusStopsUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(ids: List<Int>): StaticData {
        val initialData = repository.getStaticData()

        val filteredStops = initialData.busStops.filter { busStop -> busStop.id in ids }

        return initialData.copy(
            filteredBusStops = filteredStops
        )
    }
}
