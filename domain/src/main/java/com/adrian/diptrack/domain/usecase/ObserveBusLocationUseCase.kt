package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class ObserveBusLocationUseCase(
    private val repository: MainRepository
) {
    operator fun invoke(busId: Int? = null) = repository.observeBusLocation(busId)
}