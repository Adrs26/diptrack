package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetBusDetailUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(busId: Int) = repository.getBusById(busId)
}