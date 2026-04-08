package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetDestinationBusStopIdUseCase(
    private val repository: MainRepository
) {
    operator fun invoke() = repository.destinationBusStopId.value
}
