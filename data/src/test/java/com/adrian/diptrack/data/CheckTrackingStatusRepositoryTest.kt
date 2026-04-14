package com.adrian.diptrack.data

import com.adrian.diptrack.data.common.ModeState
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.local.dao.MainDao
import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MainRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckTrackingStatusRepositoryTest {

    private lateinit var manager: MapsManager
    private lateinit var localDataSource: MainDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: MainRepository

    private val originBusStopLocation = Coordinate(-6.9932, 110.4203)
    private val destinationBusStopLocation = Coordinate(-6.9945, 110.4230)
    private val awayFromBusStopLocation = Coordinate(-6.9800, 110.4100)
    private val currentBusId = 101
    private val seatAvailable = 10
    private val eta = 3

    @Before
    fun setUp() {
        manager = mockk()
        localDataSource = mockk()
        remoteDataSource = mockk()
        repository = MainRepository(manager, localDataSource, remoteDataSource)
    }

    @Test
    fun `checkTrackingStatus in Wait mode returns SwitchToDrive when bus arrives at origin bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)

        repository.setCurrentBusId(currentBusId)
        repository.setCurrentMode(ModeState.Wait(currentBusId))
        repository.setOriginBusStopLocation(originBusStopLocation)

        val result = repository.checkTrackingStatus(
            busLocation = originBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToDrive)
        assertTrue(repository.currentMode.value == ModeState.Drive(currentBusId))
        assertTrue(repository.previousEta.value == 0)
    }

    @Test
    fun `checkTrackingStatus in Wait mode returns BusFull when seat available is zero`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)

        repository.setCurrentMode(ModeState.Wait(currentBusId))

        val result = repository.checkTrackingStatus(
            busLocation = awayFromBusStopLocation,
            seatAvailable = 0,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.BusFull)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.previousEta.value == 0)
    }

    @Test
    fun `checkTrackingStatus in Wait mode returns Continue when bus has not arrived at origin bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)

        repository.setCurrentMode(ModeState.Wait(currentBusId))
        repository.setOriginBusStopLocation(originBusStopLocation)
        repository.setPreviousEta(eta)

        val result = repository.checkTrackingStatus(
            busLocation = awayFromBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.Continue)
        assertTrue(repository.currentMode.value is ModeState.Wait)
        assertTrue(repository.previousEta.value == eta)
    }

    @Test
    fun `checkTrackingStatus in Drive mode returns SwitchToArrive when bus arrives at destination bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(destinationBusStopLocation)

        repository.setCurrentMode(ModeState.Drive(currentBusId))
        repository.setDestinationBusStopLocation(destinationBusStopLocation)
        repository.setPreviousEta(eta)

        val result = repository.checkTrackingStatus(
            busLocation = destinationBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToArrive)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.previousEta.value == 0)
    }

    @Test
    fun `checkTrackingStatus in Drive mode returns SwitchToIdle when user is detected outside the bus`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(awayFromBusStopLocation)

        repository.setCurrentMode(ModeState.Drive(currentBusId))
        repository.setPreviousEta(eta)

        val result = repository.checkTrackingStatus(
            busLocation = destinationBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToIdle)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.previousEta.value == 0)
    }

    @Test
    fun `checkTrackingStatus in Drive mode returns Continue when bus has not arrived at destination and user is still on the bus`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(awayFromBusStopLocation)

        repository.setCurrentMode(ModeState.Drive(currentBusId))
        repository.setPreviousEta(eta)
        repository.setDestinationBusStopLocation(destinationBusStopLocation)

        val result = repository.checkTrackingStatus(
            busLocation = awayFromBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.Continue)
        assertTrue(repository.currentMode.value is ModeState.Drive)
        assertTrue(repository.previousEta.value == eta)
    }
}