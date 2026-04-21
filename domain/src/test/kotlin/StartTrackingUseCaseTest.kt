import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.common.result.StartTrackingResult
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTrackingUseCaseTest {

    private lateinit var repository: MainRepository
    private lateinit var getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase
    private lateinit var useCase: StartTrackingUseCase

    private val originBusStop = BusStop(
        id = 1,
        name = "Halte Widya Puraya",
        latitude = -6.9932,
        longitude = 110.4203
    )
    private val destinationBusStop = BusStop(
        id = 2,
        name = "Halte Teknik",
        latitude = -6.9945,
        longitude = 110.4230
    )
    private val staticData = StaticData(
        busStops = listOf(originBusStop, destinationBusStop),
        filteredBusStops = listOf(originBusStop, destinationBusStop),
        routes = emptyList(),
        error = null
    )

    private val userLocation = Coordinate(-6.9932, 110.4203)
    private val originLocation = Coordinate(originBusStop.latitude, originBusStop.longitude)
    private val destinationLocation = Coordinate(destinationBusStop.latitude, destinationBusStop.longitude)
    private val busIds = listOf(101, 102)
    private val distanceKm = 1.2
    private val filteredIds = listOf(1, 2)

    @Before
    fun setUp() {
        repository = mockk()
        getFilteredBusStopsUseCase = mockk()
        useCase = StartTrackingUseCase(repository, getFilteredBusStopsUseCase)

        every { repository.currentMode } returns MutableStateFlow(ModeState.Idle)
        every { repository.originBusStopId } returns MutableStateFlow(originBusStop.id)
        every { repository.destinationBusStopId } returns MutableStateFlow(destinationBusStop.id)
    }

    @Test
    fun `invoke returns Success when all conditions are met`() = runTest {
        val originLocation = Coordinate(originBusStop.latitude, originBusStop.longitude)

        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getStaticData() } returns staticData
        coEvery { repository.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { repository.setOriginBusStopId(originBusStop.id) } just Runs
        coEvery { repository.setOriginBusStopLocation(any()) } just Runs
        coEvery { repository.setDestinationBusStopId(destinationBusStop.id) } just Runs
        coEvery { repository.setDestinationBusStopLocation(destinationLocation) } just Runs
        coEvery { repository.calculateDistanceInKm(any(), destinationLocation) } returns distanceKm
        coEvery { repository.observeNearestAvailableBus(originBusStop.id) } returns flowOf(busIds)
        coEvery { repository.setCurrentMode(ModeState.Idle) } just Runs
        coEvery { repository.setCurrentMode(ModeState.Wait(busIds[0])) } just Runs
        coEvery { repository.setCurrentBusId(busIds[0]) } just Runs
        coEvery { getFilteredBusStopsUseCase(filteredIds) } returns staticData

        val result = useCase(destinationLocation)
        assertTrue(result is StartTrackingResult.Success)

        val success = result as StartTrackingResult.Success
        assertEquals(busIds[0], success.data.busId)
        assertEquals(originBusStop.id, success.data.originBusStopId)
        assertEquals(destinationBusStop.id, success.data.destinationBusStopId)
        assertEquals(distanceKm, success.data.distance, 0.001)
    }

    @Test
    fun `invoke returns LocationPermissionDenied when location permission is denied`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Denied

        val result = useCase(destinationLocation)
        assertTrue(result is StartTrackingResult.LocationPermissionDenied)

        coVerify(exactly = 0) { repository.getStaticData() }
        coVerify(exactly = 0) { getFilteredBusStopsUseCase(any()) }
        coVerify(exactly = 0) { repository.observeNearestAvailableBus(any()) }
    }

    @Test
    fun `invoke returns NotWithinBusStopRadius when user is not within any bus stop radius`() = runTest {
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getStaticData() } returns staticData
        coEvery { repository.calculateHaversine(any(), any()) } returns 200f

        val result = useCase(destinationLocation)

        assertTrue(result is StartTrackingResult.NotWithinBusStopRadius)

        coVerify(exactly = 0) { getFilteredBusStopsUseCase(any()) }
        coVerify(exactly = 0) { repository.observeNearestAvailableBus(any()) }
    }

    @Test
    fun `invoke returns SameBusStop when destination is the same as origin bus stop`() = runTest {
        val originLocation = Coordinate(originBusStop.latitude, originBusStop.longitude)

        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getStaticData() } returns staticData
        coEvery { repository.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { repository.setOriginBusStopId(originBusStop.id) } just Runs
        coEvery { repository.setOriginBusStopLocation(any()) } just Runs
        coEvery { repository.setDestinationBusStopId(originBusStop.id) } just Runs
        coEvery { repository.setDestinationBusStopLocation(originLocation) } just Runs

        val result = useCase(originLocation)
        assertTrue(result is StartTrackingResult.SameBusStop)

        coVerify(exactly = 0) { getFilteredBusStopsUseCase(any()) }
        coVerify(exactly = 0) { repository.observeNearestAvailableBus(any()) }
    }

    @Test
    fun `invoke returns NoAvailableBus when no active bus is available within timeout`() = runTest {
        val originLocation = Coordinate(originBusStop.latitude, originBusStop.longitude)

        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { repository.getStaticData() } returns staticData
        coEvery { repository.calculateHaversine(userLocation, originLocation) } returns 50f
        coEvery { repository.setOriginBusStopId(originBusStop.id) } just Runs
        coEvery { repository.setOriginBusStopLocation(any()) } just Runs
        coEvery { repository.setDestinationBusStopId(destinationBusStop.id) } just Runs
        coEvery { repository.setDestinationBusStopLocation(destinationLocation) } just Runs
        coEvery { repository.calculateDistanceInKm(any(), destinationLocation) } returns distanceKm
        coEvery { repository.observeNearestAvailableBus(originBusStop.id) } returns flowOf(emptyList())
        coEvery { getFilteredBusStopsUseCase(filteredIds) } returns staticData

        val result = useCase(destinationLocation)
        assertTrue(result is StartTrackingResult.NoAvailableBus)

        coVerify(exactly = 0) { repository.setCurrentMode(any()) }
        coVerify(exactly = 0) { repository.setCurrentBusId(any()) }
    }
}