import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.CheckTrackingStatusUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckTrackingStatusUseCaseTest {

    private lateinit var repository: MainRepository
    private lateinit var useCase: CheckTrackingStatusUseCase

    private val originBusStopLocation = Coordinate(-6.9932, 110.4203)
    private val destinationBusStopLocation = Coordinate(-6.9945, 110.4230)
    private val awayFromBusStopLocation = Coordinate(-6.9800, 110.4100)
    private val currentBusId = 101
    private val seatAvailable = 10
    private val eta = 3

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CheckTrackingStatusUseCase(repository)
    }

    @Test
    fun `invoke in Wait mode returns SwitchToDrive when bus arrives at origin bus stop`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(0)
        every { repository.originBusStopLocation } returns MutableStateFlow(originBusStopLocation)
        every { repository.currentBusId } returns MutableStateFlow(currentBusId)
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)
        coEvery { repository.setCurrentMode(ModeState.Drive(currentBusId)) } just Runs
        coEvery { repository.setPreviousEta(0) } just Runs

        val result = useCase(
            busLocation = originBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToDrive)
        coVerify(exactly = 1) { repository.setCurrentMode(ModeState.Drive(currentBusId)) }
        coVerify(exactly = 1) { repository.setPreviousEta(0) }
    }

    @Test
    fun `invoke in Wait mode returns BusFull when seat available is zero`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(0)
        every { repository.setCurrentMode(ModeState.Idle) } just Runs
        every { repository.setPreviousEta(0) } just Runs
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)

        val result = useCase(
            busLocation = awayFromBusStopLocation,
            seatAvailable = 0,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.BusFull)
        coVerify(exactly = 1) { repository.setCurrentMode(ModeState.Idle) }
        coVerify(exactly = 1) { repository.setPreviousEta(0) }
    }

    @Test
    fun `invoke in Wait mode returns Continue when bus has not arrived at origin bus stop`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Wait(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(eta)
        every { repository.originBusStopLocation } returns MutableStateFlow(originBusStopLocation)
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)
        coEvery { repository.setPreviousEta(eta) } just Runs

        val result = useCase(
            busLocation = awayFromBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.Continue)
        coVerify(exactly = 1) { repository.setPreviousEta(eta) }
        coVerify(exactly = 0) { repository.setCurrentMode(any()) }
    }

    @Test
    fun `invoke in Drive mode returns SwitchToArrive when bus arrives at destination bus stop`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(0)
        every { repository.destinationBusStopLocation } returns MutableStateFlow(destinationBusStopLocation)
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(destinationBusStopLocation)
        coEvery { repository.setCurrentMode(ModeState.Idle) } just Runs
        coEvery { repository.setPreviousEta(0) } just Runs

        val result = useCase(
            busLocation = destinationBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToArrive)
        coVerify(exactly = 1) { repository.setCurrentMode(ModeState.Idle) }
        coVerify(exactly = 1) { repository.setPreviousEta(0) }
    }

    @Test
    fun `invoke in Drive mode returns SwitchToIdle when user is detected outside the bus`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(0)
        every { repository.setCurrentMode(ModeState.Idle) } just Runs
        every { repository.setPreviousEta(0) } just Runs
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(originBusStopLocation)

        val result = useCase(
            busLocation = awayFromBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.SwitchToIdle)
        coVerify(exactly = 1) { repository.setCurrentMode(ModeState.Idle) }
        coVerify(exactly = 1) { repository.setPreviousEta(0) }
    }

    @Test
    fun `invoke in Drive mode returns Continue when bus has not arrived at destination and user is still on the bus`() = runTest {
        every { repository.currentMode } returns MutableStateFlow(ModeState.Drive(currentBusId))
        every { repository.previousEta } returns MutableStateFlow(eta)
        every { repository.destinationBusStopLocation } returns MutableStateFlow(destinationBusStopLocation)
        coEvery { repository.getCurrentUserLocation() } returns MapsPermissionResult.Granted(awayFromBusStopLocation)
        coEvery { repository.setPreviousEta(eta) } just Runs

        val result = useCase(
            busLocation = awayFromBusStopLocation,
            seatAvailable = seatAvailable,
            eta = eta
        )

        assertTrue(result is CheckTrackingStatusResult.Continue)
        coVerify(exactly = 1) { repository.setPreviousEta(eta) }
        coVerify(exactly = 0) { repository.setCurrentMode(any()) }
    }
}