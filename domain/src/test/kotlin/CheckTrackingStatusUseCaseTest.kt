import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.CheckTrackingStatusUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CheckTrackingStatusUseCaseTest {

    private val repository = mockk<MainRepository>(relaxed = true)
    private val getCurrentUserLocationUseCase = mockk<GetCurrentUserLocationUseCase>()

    private lateinit var useCase: CheckTrackingStatusUseCase

    private val busId = 1
    private val userLocation = Coordinate(1.0, 1.0)
    private val nearLocation = Coordinate(1.0, 1.0)
    private val farLocation = Coordinate(2.0, 2.0)

    @Before
    fun setup() {
        useCase = CheckTrackingStatusUseCase(repository, getCurrentUserLocationUseCase)

        coEvery { getCurrentUserLocationUseCase.invoke() } returns MapsPermissionResult.Granted(userLocation)

        every { repository.previousEta } returns MutableStateFlow(0)
        every { repository.currentBusId } returns MutableStateFlow(busId)
    }

    @Test
    fun `WAIT to DRIVE when bus arrives at origin`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(busId))
        every { repository.originBusStopLocation } returns MutableStateFlow(nearLocation)

        val result = useCase(
            busLocation = nearLocation,
            seatAvailable = 5,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.SwitchToDrive, result)
        verify { repository.setCurrentMode(match { it is ModeState.Drive }) }
        verify { repository.setPreviousEta(0) }
    }

    @Test
    fun `WAIT to BUS FULL`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(busId))

        val result = useCase(
            busLocation = nearLocation,
            seatAvailable = 0,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.BusFull, result)
    }

    @Test
    fun `WAIT to CONTINUE when bus not at origin`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(busId))
        every { repository.originBusStopLocation } returns MutableStateFlow(nearLocation)

        val result = useCase(
            busLocation = farLocation,
            seatAvailable = 5,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.Continue, result)
        verify { repository.setPreviousEta(10) }
    }

    @Test
    fun `DRIVE to ARRIVE when reach destination`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(busId))
        every { repository.destinationBusStopLocation } returns MutableStateFlow(nearLocation)

        val result = useCase(
            busLocation = nearLocation,
            seatAvailable = 5,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.SwitchToArrive, result)
        verify { repository.setCurrentMode(ModeState.Idle) }
        verify { repository.setPreviousEta(0) }
    }

    @Test
    fun `DRIVE to CANCEL when user leaves bus`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(busId))

        val result = useCase(
            busLocation = farLocation,
            seatAvailable = 5,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.SwitchToIdle, result)
    }

    @Test
    fun `DRIVE to CONTINUE when still on trip`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(busId))
        every { repository.destinationBusStopLocation } returns MutableStateFlow(farLocation)

        val result = useCase(
            busLocation = nearLocation,
            seatAvailable = 5,
            eta = 10
        )

        assertEquals(CheckTrackingStatusResult.Continue, result)
        verify { repository.setPreviousEta(10) }
    }
}