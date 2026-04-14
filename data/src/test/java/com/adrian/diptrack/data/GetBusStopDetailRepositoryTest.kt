package com.adrian.diptrack.data

import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.common.result.GetBusStopDetailResult
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.local.dao.MainDao
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.projection.BusEtaProjection
import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetBusStopDetailRepositoryTest {

    private lateinit var manager: MapsManager
    private lateinit var localDataSource: MainDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: MainRepository

    private val busStopId = 1
    private val userLocation = Coordinate(-6.9932, 110.4203)
    private val busStop = BusStopEntity(
        id = 1,
        name = "Halte Widya Puraya",
        latitude = -6.9945,
        longitude = 110.4230
    )
    private val busStopLocation = Coordinate(busStop.latitude, busStop.longitude)
    private val distanceKm = 0.35
    private val busEtas = listOf(
        BusEtaProjection(
            busCode = "B1",
            licensePlate = "1234",
            eta = 3
        ),
        BusEtaProjection(
            busCode = "B2",
            licensePlate = "2345",
            eta = 7
        )
    )

    @Before
    fun setUp() {
        manager = mockk()
        localDataSource = mockk()
        remoteDataSource = mockk()
        repository = MainRepository(manager, localDataSource, remoteDataSource)
    }

    @Test
    fun `getBusStopDetail returns Success when bus stop data is available`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm
        coEvery { localDataSource.getBusStopById(busStopId) } returns busStop
        coEvery { localDataSource.getBusEtaForBusStop(busStopId) } returns busEtas

        val result = repository.getBusStopDetail(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertEquals("Halte Widya Puraya", success.busStopDetail.busStopName)
    }

    @Test
    fun `getBusStopDetail returns LocationPermissionDenied when location permission is denied`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Denied

        val result = repository.getBusStopDetail(busStopId)
        assertTrue(result is GetBusStopDetailResult.LocationPermissionDenied)

        coVerify(exactly = 0) { localDataSource.getBusStopById(any()) }
        coVerify(exactly = 0) { localDataSource.getBusEtaForBusStop(any()) }
        coVerify(exactly = 0) { manager.calculateDistanceInKm(any(), any()) }
    }

    @Test
    fun `getBusStopDetail returns correct distance from user location to bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm
        coEvery { localDataSource.getBusStopById(busStopId) } returns busStop
        coEvery { localDataSource.getBusEtaForBusStop(busStopId) } returns busEtas

        val result = repository.getBusStopDetail(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertEquals(distanceKm, success.busStopDetail.distanceKilometers, 0.001)
    }

    @Test
    fun `getBusStopDetail returns bus etas when there are active buses heading to bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm
        coEvery { localDataSource.getBusStopById(busStopId) } returns busStop
        coEvery { localDataSource.getBusEtaForBusStop(busStopId) } returns busEtas

        val result = repository.getBusStopDetail(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertTrue(success.busStopDetail.busEtas.isNotEmpty())
        assertEquals(2, success.busStopDetail.busEtas.size)
        assertEquals(3, success.busStopDetail.busEtas[0].eta)
        assertEquals(7, success.busStopDetail.busEtas[1].eta)
    }

    @Test
    fun `getBusStopDetail returns empty bus etas when no active buses heading to bus stop`() = runTest {
        coEvery { manager.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { manager.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm
        coEvery { localDataSource.getBusStopById(busStopId) } returns busStop
        coEvery { localDataSource.getBusEtaForBusStop(busStopId) } returns emptyList()

        val result = repository.getBusStopDetail(busStopId)

        assertTrue(result is GetBusStopDetailResult.Success)
        val success = result as GetBusStopDetailResult.Success
        assertTrue(success.busStopDetail.busEtas.isEmpty())
    }
}