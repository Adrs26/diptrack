package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class ObserveBusArrivalInfoUseCase(
    private val repository: MainRepository
) {
    operator fun invoke(busId: Int, busStopId: Int) = repository.observeBusArrivalInfo(busId, busStopId)
}