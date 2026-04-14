package com.adrian.diptrack.data

import com.adrian.diptrack.data.common.ModeState
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.common.result.StartTrackingResult
import com.adrian.diptrack.data.local.dao.MainDao
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.remote.response.BusResponse
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.remote.source.Result
import com.adrian.diptrack.data.repository.MainRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTrackingRepositoryTest {

    private lateinit var manager: MapsManager
    private lateinit var localDataSource: MainDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: MainRepository

    private val originBusStop = BusStopEntity(
        id = 1,
        name = "Halte Widya Puraya",
        latitude = -6.9932,
        longitude = 110.4203
    )
    private val destinationBusStop = BusStopEntity(
        id = 2,
        name = "Halte Teknik",
        latitude = -6.9945,
        longitude = 110.4230
    )
    private val busStopResponses = listOf(
        BusStopResponse(
            id = 1,
            name = "Halte Widya Puraya",
            latitude = -6.9932,
            longitude = 110.4203
        ),
        BusStopResponse(
            id = 2,
            name = "Halte Teknik",
            latitude = -6.9945,
            longitude = 110.4230
        )
    )
    private val busResponse = listOf(
        BusResponse(
            id = 101,
            licensePlate = "1234",
            busCode = "B1",
            busType = "Mini",
            status = "Berjalan",
            capacity = 30,
            driver = "Andi"
        ),
        BusResponse(
            id = 102,
            licensePlate = "2345",
            busCode = "B2",
            busType = "Medium",
            status = "Berjalan",
            capacity = 40,
            driver = "Budi"
        )
    )

    private val userLocation = Coordinate(-6.9932, 110.4203)
    private val originLocation = Coordinate(originBusStop.latitude, originBusStop.longitude)
    private val destinationLocation = Coordinate(destinationBusStop.latitude, destinationBusStop.longitude)
    private val busIds = listOf(101, 102)
    private val distanceKm = 1.2

    @Before
    fun setUp() {
        manager = mockk()
        localDataSource = mockk()
        remoteDataSource = mockk()
        repository = MainRepository(manager, localDataSource, remoteDataSource)
    }

    @Test
    fun `startTracking returns Success when all conditions are met`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { manager.calculateDistanceInKm(any(), destinationLocation) } returns distanceKm
        coEvery { remoteDataSource.getBusStops() } returns Result.Success(busStopResponses)
        coEvery { remoteDataSource.getRoutes() } returns Result.Success(emptyList())
        coEvery { remoteDataSource.getBuses() } returns Result.Success(busResponse)
        coEvery { localDataSource.syncInitialData(any(), any(), any()) } just Runs
        coEvery { localDataSource.getNearestAvailableBus(originBusStop.id) } returns flowOf(busIds)

        val result = repository.startTracking(destinationLocation)
        assertTrue(result is StartTrackingResult.Success)

        val success = result as StartTrackingResult.Success
        assertEquals(busIds[0], success.data.busId)
        assertEquals(originBusStop.id, success.data.originBusStopId)
        assertEquals(destinationBusStop.id, success.data.destinationBusStopId)
        assertEquals(distanceKm, success.data.distance, 0.001)
        assertTrue(repository.currentMode.value == ModeState.Wait(success.data.busId))
        assertTrue(repository.currentBusId.value == success.data.busId)
    }

    @Test
    fun `startTracking returns LocationPermissionDenied when location permission is denied`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Denied

        val result = repository.startTracking(destinationLocation)
        assertTrue(result is StartTrackingResult.LocationPermissionDenied)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.currentBusId.value == 0)

        coVerify(exactly = 0) { remoteDataSource.getBusStops() }
        coVerify(exactly = 0) { remoteDataSource.getRoutes() }
        coVerify(exactly = 0) { remoteDataSource.getBuses() }
        coVerify(exactly = 0) { localDataSource.syncInitialData(any(), any(), any()) }
        coVerify(exactly = 0) { localDataSource.getNearestAvailableBus(any()) }
    }

    @Test
    fun `startTracking returns NotWithinBusStopRadius when user is not within any bus stop radius`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateHaversine(any(), any()) } returns 200f
        coEvery { remoteDataSource.getBusStops() } returns Result.Success(busStopResponses)
        coEvery { remoteDataSource.getRoutes() } returns Result.Success(emptyList())
        coEvery { remoteDataSource.getBuses() } returns Result.Success(busResponse)
        coEvery { localDataSource.syncInitialData(any(), any(), any()) } just Runs

        val result = repository.startTracking(destinationLocation)
        assertTrue(result is StartTrackingResult.NotWithinBusStopRadius)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.currentBusId.value == 0)

        coVerify(exactly = 1) { remoteDataSource.getBusStops() }
        coVerify(exactly = 1) { remoteDataSource.getRoutes() }
        coVerify(exactly = 1) { remoteDataSource.getBuses() }
        coVerify(exactly = 1) { localDataSource.syncInitialData(any(), any(), any()) }
        coVerify(exactly = 0) { localDataSource.getNearestAvailableBus(any()) }
    }

    @Test
    fun `startTracking returns SameBusStop when destination is the same as origin bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { remoteDataSource.getBusStops() } returns Result.Success(busStopResponses)
        coEvery { remoteDataSource.getRoutes() } returns Result.Success(emptyList())
        coEvery { remoteDataSource.getBuses() } returns Result.Success(busResponse)
        coEvery { localDataSource.syncInitialData(any(), any(), any()) } just Runs

        val result = repository.startTracking(originLocation)
        assertTrue(result is StartTrackingResult.SameBusStop)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.currentBusId.value == 0)

        coVerify(exactly = 1) { remoteDataSource.getBusStops() }
        coVerify(exactly = 1) { remoteDataSource.getRoutes() }
        coVerify(exactly = 1) { remoteDataSource.getBuses() }
        coVerify(exactly = 1) { localDataSource.syncInitialData(any(), any(), any()) }
        coVerify(exactly = 0) { localDataSource.getNearestAvailableBus(any()) }
    }

    @Test
    fun `startTracking returns NoAvailableBus when no active bus is available within timeout`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { manager.calculateDistanceInKm(any(), destinationLocation) } returns distanceKm
        coEvery { remoteDataSource.getBusStops() } returns Result.Success(busStopResponses)
        coEvery { remoteDataSource.getRoutes() } returns Result.Success(emptyList())
        coEvery { remoteDataSource.getBuses() } returns Result.Success(busResponse)
        coEvery { localDataSource.syncInitialData(any(), any(), any()) } just Runs
        coEvery { localDataSource.getNearestAvailableBus(originBusStop.id) } returns flowOf(emptyList())

        val result = repository.startTracking(destinationLocation)
        assertTrue(result is StartTrackingResult.NoAvailableBus)
        assertTrue(repository.currentMode.value is ModeState.Idle)
        assertTrue(repository.currentBusId.value == 0)
    }
}