package com.adrian.diptrack.domain.usecase

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import kotlin.math.abs

class CheckTrackingStatusUseCase(
    private val repository: MainRepository,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
) {
    suspend operator fun invoke(busLocation: Coordinate, seatAvailable: Int, eta: Int): CheckTrackingStatusResult {
        val userLocation = when (val result = getCurrentUserLocationUseCase()) {
            is MapsPermissionResult.Denied -> return CheckTrackingStatusResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        when (repository.currentMode.value) {
            is ModeState.Drive -> {
                if (!userLocation.isNear(busLocation)) return CheckTrackingStatusResult.SwitchToIdle
            }
            else -> Unit
        }

        if (seatAvailable == 0) return CheckTrackingStatusResult.BusFull

        val isEtaBounce = eta.isBounce(repository.previousEta.value)
        val isBusArrived = when (repository.currentMode.value) {
            is ModeState.Wait -> busLocation.isNear(repository.originBusStopLocation.value)
            is ModeState.Drive -> busLocation.isNear(repository.destinationBusStopLocation.value)
            else -> false
        }

        if (isEtaBounce || isBusArrived) {
            when (repository.currentMode.value) {
                is ModeState.Wait -> {
                    repository.setCurrentMode(ModeState.Drive(repository.currentBusId.value))
                    repository.setPreviousEta(0)
                    return CheckTrackingStatusResult.SwitchToDrive
                }
                is ModeState.Drive -> {
                    repository.setCurrentMode(ModeState.Idle)
                    repository.setPreviousEta(0)
                    return CheckTrackingStatusResult.SwitchToArrive
                }
                else -> Unit
            }
        }

        repository.setPreviousEta(eta)
        return CheckTrackingStatusResult.Continue
    }

    private fun Int.isBounce(other: Int): Boolean {
        return other != 0 && this > other && this - other > 1
    }

    private fun Coordinate.isNear(other: Coordinate, threshold: Double = 1e-4): Boolean {
        val latDiff = abs(this.latitude - other.latitude)
        val lonDiff = abs(this.longitude - other.longitude)

        return latDiff < threshold && lonDiff < threshold
    }
}