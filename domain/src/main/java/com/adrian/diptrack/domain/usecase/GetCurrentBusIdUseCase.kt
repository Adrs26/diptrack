package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetCurrentBusIdUseCase(
    private val repository: MainRepository
) {
    operator fun invoke() = repository.currentBusId.value
}