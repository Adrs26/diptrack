package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.repository.MainRepository

class GetCurrentUserLocationUseCase(private val repository: MainRepository) {
    suspend operator fun invoke() = repository.getCurrentUserLocation()
}