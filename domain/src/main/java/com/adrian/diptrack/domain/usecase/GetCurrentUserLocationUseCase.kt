package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.manager.MapsManager

class GetCurrentUserLocationUseCase(
    private val manager: MapsManager
) {
    suspend operator fun invoke() = manager.getCurrentUserLocation()
}