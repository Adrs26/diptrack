package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.repository.MainRepository

class CancelTrackingUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(): List<BusStop> {
        repository.setCurrentMode(ModeState.Idle)
        return repository.getStaticData().busStops
    }
}