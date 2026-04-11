package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.data.common.ModeState
import com.adrian.diptrack.data.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.data.common.result.GetBusStopDetailResult
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.common.result.StartTrackingResult
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.repository.MainRepository
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.adrian.diptrack.presentation.screen.util.toState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import kotlin.math.ceil

class MainViewModel(
    private val networkObserver: NetworkStatusObserver,
    private val repository: MainRepository
) : ViewModel(), ContainerHost<MainState, MainSideEffect> {

    override val container = container<MainState, MainSideEffect>(MainState())

    private val busStopIds = mutableListOf<Int>()

    init {
        observeNetwork()
        observeCurrentMode()
        observeBusLocations()
        observeBusArrivalInfo()
        triggerInitialData()
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.OnAnimateToUserLocation -> animateToUserLocation()
            is MainAction.OnRefresh -> triggerInitialData()
            is MainAction.OnBusStopClicked -> getBusStopDetail(action.busStopId)
            is MainAction.OnBusClicked -> getBusDetail(action.busId)
            is MainAction.OnStartTracking -> startTracking(action.destinationLocation)
            is MainAction.OnCancelTracking -> cancelTracking()
        }
    }

    private fun observeNetwork() = intent {
        networkObserver.isConnected.collect {
            triggerInitialData()
        }
    }

    private fun observeCurrentMode() = intent {
        repository.currentMode.collect { mode ->
            reduce { state.copy(currentMode = mode) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBusLocations() = intent {
        container.stateFlow
            .map { it.currentMode }
            .distinctUntilChanged()
            .flatMapLatest { mode ->
                when (mode) {
                    is ModeState.Idle, ModeState.Arrive -> repository.observeBusLocation(null)
                    is ModeState.Wait -> repository.observeBusLocation(mode.busId)
                    is ModeState.Drive -> repository.observeBusLocation(mode.busId)
                }
            }
            .collect { busLocations ->
                reduce { state.copy(busLocations = busLocations) }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBusArrivalInfo() = intent {
        container.stateFlow
            .map { it.currentMode }
            .distinctUntilChanged()
            .flatMapLatest { mode ->
                val busId = repository.currentBusId.value
                val originId = repository.originBusStopId.value
                val destinationId = repository.destinationBusStopId.value

                when (mode) {
                    is ModeState.Idle, ModeState.Arrive -> repository.observeBusArrivalInfo(0, 0)
                    is ModeState.Wait -> repository.observeBusArrivalInfo(busId, originId)
                    is ModeState.Drive -> repository.observeBusArrivalInfo(busId, destinationId)
                }
            }
            .collect { busArrivalInfo ->
                busArrivalInfo?.let { busArrivalInfo ->
                    val result = repository.checkTrackingStatus(
                        busLocation = Coordinate(busArrivalInfo.latitude, busArrivalInfo.longitude),
                        seatAvailable = busArrivalInfo.maxCapacity - busArrivalInfo.passenger,
                        eta = ceil(busArrivalInfo.eta / 60.0).toInt()
                    )

                    when (result) {
                        is CheckTrackingStatusResult.LocationPermissionDenied -> {
                            postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
                        }
                        is CheckTrackingStatusResult.BusFull -> {
                            startTracking(repository.destinationBusStopLocation.value)
                            postSideEffect(MainSideEffect.ShowBusFullWarning)
                        }
                        is CheckTrackingStatusResult.SwitchToIdle -> {
                            postSideEffect(MainSideEffect.ShowCancelTrackingNotification)
                        }
                        is CheckTrackingStatusResult.SwitchToDrive -> {
                            reduce { state.copy(busArrivalInfoState = busArrivalInfo.toState()) }
                        }
                        is CheckTrackingStatusResult.SwitchToArrive -> {
                            cancelTracking()
                            postSideEffect(MainSideEffect.ShowArrivalNotification)
                        }
                        is CheckTrackingStatusResult.Continue -> {
                            reduce { state.copy(busArrivalInfoState = busArrivalInfo.toState()) }
                        }
                    }
                }
            }
    }

    private fun triggerInitialData() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val result = when (state.currentMode) {
            is ModeState.Idle, ModeState.Arrive -> repository.getStaticData()
            else -> repository.getFilteredBusStops(busStopIds)
        }

        reduce {
            state.copy(
                busStops = result.busStops,
                filteredBusStops = result.filteredBusStops,
                routes = result.routes,
                isLoading = false,
                error = result.error
            )
        }
    }

    private fun animateToUserLocation() = intent {
        when (val result = repository.getCurrentUserLocation()) {
            is MapsPermissionResult.Denied -> {
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is MapsPermissionResult.Granted -> {
                val coordinate = result.location
                val cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), 16f)
                postSideEffect(MainSideEffect.AnimateCamera(cameraUpdate))
            }
        }
    }

    private fun getBusDetail(busId: Int) = intent {
        val busDetail = repository.getBusById(busId)
        reduce { state.copy(busState = busDetail.toState()) }
        postSideEffect(MainSideEffect.ShowBusDetailDialog)
    }

    private fun getBusStopDetail(busStopId: Int) = intent {
        when (val result = repository.getBusStopDetail(busStopId)) {
            is GetBusStopDetailResult.LocationPermissionDenied -> {
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is GetBusStopDetailResult.Success -> {
                reduce { state.copy(busStopState = result.busStopDetail.toState()) }
                postSideEffect(MainSideEffect.ShowBusStopDetailDialog)
            }
        }
    }

    private fun startTracking(destinationLocation: Coordinate) = intent {
        reduce { state.copy(isLoading = true, error = null) }

        when (val result = repository.startTracking(destinationLocation)) {
            is StartTrackingResult.LocationPermissionDenied -> {
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is StartTrackingResult.ChangeDestinationRejected -> {
                postSideEffect(MainSideEffect.ShowChangeDestinationRejectedWarning)
            }
            is StartTrackingResult.NotWithinBusStopRadius -> {
                postSideEffect(MainSideEffect.ShowOutsideBusStopWarning)
            }
            is StartTrackingResult.SameBusStop -> {
                postSideEffect(MainSideEffect.ShowSameBusStopWarning)
            }
            is StartTrackingResult.NoAvailableBus -> {
                postSideEffect(MainSideEffect.ShowNoAvailableBusWarning)
            }
            is StartTrackingResult.Success -> {
                busStopIds.clear()
                busStopIds.add(result.data.originBusStopId)
                busStopIds.add(result.data.destinationBusStopId)

                reduce {
                    state.copy(
                        filteredBusStops = result.data.busStops,
                        distanceKilometers = result.data.distance,
                        originName = result.data.originBusStopName,
                        destinationName = result.data.destinationBusStopName
                    )
                }
                animateToUserLocation()
            }
        }

        reduce { state.copy(isLoading = false) }
    }

    private fun cancelTracking() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val busStops = repository.cancelTracking()

        reduce {
            state.copy(
                filteredBusStops = busStops,
                isLoading = false
            )
        }
        animateToUserLocation()
    }
}