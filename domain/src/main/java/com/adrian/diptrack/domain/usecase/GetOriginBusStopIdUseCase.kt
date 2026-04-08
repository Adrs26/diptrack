package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetOriginBusStopIdUseCase(
    private val repository: MainRepository
) {
    operator fun invoke() = repository.originBusStopId.value
}