import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.common.result.StartTrackingResult
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTrackingUseCaseTest {

    private val manager = mockk<MapsManager>(relaxed = true)
    private val repository = mockk<MainRepository>(relaxed = true)
    private val getCurrentUserLocationUseCase = mockk<GetCurrentUserLocationUseCase>()
    private val getFilteredBusStopsUseCase = mockk<GetFilteredBusStopsUseCase>()

    private lateinit var useCase: StartTrackingUseCase

    private val userLocation = Coordinate(1.0, 1.0)
    private val destinationLocation = Coordinate(2.0, 2.0)

    private val originBusStop = BusStop(1, "Origin", 1.0, 1.0)
    private val destinationBusStop = BusStop(2, "Destination", 2.0, 2.0)

    private val staticData = StaticData(
        busStops = listOf(originBusStop, destinationBusStop),
        filteredBusStops = listOf(originBusStop, destinationBusStop),
        routes = emptyList(),
        error = null
    )

    @Before
    fun setup() {
        useCase = StartTrackingUseCase(manager, repository, getCurrentUserLocationUseCase, getFilteredBusStopsUseCase)

        every { repository.currentMode } returns MutableStateFlow(ModeState.Idle)
        every { repository.originBusStopId } returns MutableStateFlow(1)
        every { repository.destinationBusStopId } returns MutableStateFlow(2)
        coEvery { repository.getStaticData() } returns staticData

        every { manager.calculateHaversine(any(), any()) } returns 0.0F
        every { manager.calculateDistanceInKm(any(), any()) } returns 5.0

        coEvery { getCurrentUserLocationUseCase() } returns MapsPermissionResult.Granted(userLocation)
        coEvery { getFilteredBusStopsUseCase(any()) } returns staticData
    }

    @Test
    fun `SUCCESS when all conditions met`() = runTest {
        val flow = flowOf(listOf(10))

        every { repository.observeNearestAvailableBus(any()) } returns flow

        val result = useCase(destinationLocation)

        assertTrue(result is StartTrackingResult.Success)

        verify { repository.setCurrentMode(ModeState.Idle) }
        verify { repository.setCurrentMode(match { it is ModeState.Wait }) }
        verify { repository.setCurrentBusId(10) }
    }

    @Test
    fun `FAIL when location permission denied`() = runTest {
        coEvery { getCurrentUserLocationUseCase() } returns MapsPermissionResult.Denied

        val result = useCase(destinationLocation)

        assertEquals(StartTrackingResult.LocationPermissionDenied, result)
    }

    @Test
    fun `FAIL when user not in bus stop radius`() = runTest {
        every { manager.calculateHaversine(any(), any()) } returns 999.0f

        val result = useCase(destinationLocation)

        assertEquals(StartTrackingResult.NotWithinBusStopRadius, result)
    }

    @Test
    fun `FAIL when origin and destination are same`() = runTest {
        val sameLocation = Coordinate(1.0, 1.0)

        val result = useCase(sameLocation)

        assertEquals(StartTrackingResult.SameBusStop, result)
    }

    @Test
    fun `FAIL when no available bus`() = runTest {
        val emptyFlow = flowOf(emptyList<Int>())

        every { repository.observeNearestAvailableBus(any()) } returns emptyFlow

        val result = useCase(destinationLocation)

        assertEquals(StartTrackingResult.NoAvailableBus, result)
    }
}