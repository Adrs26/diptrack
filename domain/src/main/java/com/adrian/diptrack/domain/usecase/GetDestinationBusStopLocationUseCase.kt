package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetDestinationBusStopLocationUseCase(
    private val repository: MainRepository
) {
    operator fun invoke() = repository.destinationBusStopLocation.value
}