import com.adrian.diptrack.domain.common.result.GetBusStopDetailResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetBusStopDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetBusStopDetailUseCaseTest {

    private lateinit var repository: MainRepository
    private lateinit var useCase: GetBusStopDetailUseCase

    private val busStopId = 1
    private val userLocation = Coordinate(-6.9932, 110.4203)
    private val busStop = BusStop(
        id = 1,
        name = "Halte Widya Puraya",
        latitude = -6.9945,
        longitude = 110.4230
    )
    private val busStopLocation = Coordinate(busStop.latitude, busStop.longitude)
    private val distanceKm = 0.35
    private val busEtas = listOf(
        BusEta(
            busCode = "B1",
            licensePlate = "1234",
            etaMinutes = 3
        ),
        BusEta(
            busCode = "B2",
            licensePlate = "2345",
            etaMinutes = 7
        )
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetBusStopDetailUseCase(repository)
    }

    @Test
    fun `invoke returns Success when bus stop data is available`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getBusStopById(busStopId) } returns busStop
        coEvery { repository.getBusEtaForBusStop(busStopId) } returns busEtas
        coEvery { repository.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm

        val result = useCase(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertEquals("Halte Widya Puraya", success.busStopDetail.busStopName)
    }

    @Test
    fun `invoke returns LocationPermissionDenied when location permission is denied`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Denied

        val result = useCase(busStopId)
        assertTrue(result is GetBusStopDetailResult.LocationPermissionDenied)

        coVerify(exactly = 0) { repository.getBusStopById(any()) }
        coVerify(exactly = 0) { repository.getBusEtaForBusStop(any()) }
        coVerify(exactly = 0) { repository.calculateDistanceInKm(any(), any()) }
    }

    @Test
    fun `invoke returns correct distance from user location to bus stop`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getBusStopById(busStopId) } returns busStop
        coEvery { repository.getBusEtaForBusStop(busStopId) } returns busEtas
        coEvery { repository.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm

        val result = useCase(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertEquals(distanceKm, success.busStopDetail.distanceKilometers, 0.001)
    }

    @Test
    fun `invoke returns bus etas when there are active buses heading to bus stop`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getBusStopById(busStopId) } returns busStop
        coEvery { repository.getBusEtaForBusStop(busStopId) } returns busEtas
        coEvery { repository.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm

        val result = useCase(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertTrue(success.busStopDetail.busEtas.isNotEmpty())
        assertEquals(2, success.busStopDetail.busEtas.size)
        assertEquals(3, success.busStopDetail.busEtas[0].etaMinutes)
        assertEquals(7, success.busStopDetail.busEtas[1].etaMinutes)
    }

    @Test
    fun `invoke returns empty bus etas when no active buses heading to bus stop`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getBusStopById(busStopId) } returns busStop
        coEvery { repository.getBusEtaForBusStop(busStopId) } returns emptyList()
        coEvery { repository.calculateDistanceInKm(userLocation, busStopLocation) } returns distanceKm

        val result = useCase(busStopId)
        assertTrue(result is GetBusStopDetailResult.Success)

        val success = result as GetBusStopDetailResult.Success
        assertTrue(success.busStopDetail.busEtas.isEmpty())
    }
}