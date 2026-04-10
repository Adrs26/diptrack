import com.adrian.diptrack.domain.common.result.GetBusStopDetailResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetBusStopDetailUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetBusStopDetailUseCaseTest {

    private val manager = mockk<MapsManager>()
    private val repository = mockk<MainRepository>()
    private val getCurrentUserLocationUseCase= mockk<GetCurrentUserLocationUseCase>()

    private lateinit var useCase: GetBusStopDetailUseCase

    private val userLocation = Coordinate(1.0, 1.0)
    private val busStop = BusStop(1, "Halte A", 2.0, 2.0)

    @Before
    fun setup() {
        useCase = GetBusStopDetailUseCase(manager, repository, getCurrentUserLocationUseCase)

        coEvery { getCurrentUserLocationUseCase() } returns MapsPermissionResult.Granted(userLocation)
    }

    @Test
    fun `SUCCESS when bus stop data available`() = runTest {
        val etas = listOf(BusEta("Bus 1", "123", 5), BusEta("Bus 2", "234", 10))

        coEvery { repository.getBusStopById(1) } returns busStop
        coEvery { repository.getBusEtaForBusStop(1) } returns etas

        every { manager.calculateDistanceInKm(any(), any()) } returns 3.5

        val result = useCase(1)

        assertTrue(result is GetBusStopDetailResult.Success)

        val data = (result as GetBusStopDetailResult.Success).busStopDetail
        assertEquals("Halte A", data.busStopName)
    }

    @Test
    fun `FAIL when location permission denied`() = runTest {
        coEvery { getCurrentUserLocationUseCase() } returns MapsPermissionResult.Denied

        val result = useCase(1)

        assertEquals(GetBusStopDetailResult.LocationPermissionDenied, result)
    }

    @Test
    fun `SUCCESS calculate distance correctly`() = runTest {
        coEvery { repository.getBusStopById(1) } returns busStop
        coEvery { repository.getBusEtaForBusStop(1) } returns emptyList()

        every {
            manager.calculateDistanceInKm(userLocation, any())
        } returns 7.2

        val result = useCase(1)

        val data = (result as GetBusStopDetailResult.Success).busStopDetail
        assertEquals(7.2, data.distanceKilometers, 0.0)
    }

    @Test
    fun `SUCCESS when bus ETA available`() = runTest {
        val etas = listOf(BusEta("Bus 1", "123", 5), BusEta("Bus 2", "234", 10))

        coEvery { repository.getBusStopById(1) } returns busStop
        coEvery { repository.getBusEtaForBusStop(1) } returns etas

        every { manager.calculateDistanceInKm(any(), any()) } returns 2.0

        val result = useCase(1)

        val data = (result as GetBusStopDetailResult.Success).busStopDetail
        assertEquals(etas, data.busEtas)
    }

    @Test
    fun `SUCCESS when no bus ETA available`() = runTest {
        coEvery { repository.getBusStopById(1) } returns busStop
        coEvery { repository.getBusEtaForBusStop(1) } returns emptyList()

        every { manager.calculateDistanceInKm(any(), any()) } returns 2.0

        val result = useCase(1)

        val data = (result as GetBusStopDetailResult.Success).busStopDetail
        assertTrue(data.busEtas.isEmpty())
    }
}