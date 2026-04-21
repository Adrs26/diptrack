package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.domain.common.result.GetBusStopDetailResult
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.common.result.StartTrackingResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.usecase.CancelTrackingUseCase
import com.adrian.diptrack.domain.usecase.CheckTrackingStatusUseCase
import com.adrian.diptrack.domain.usecase.GetBusDetailUseCase
import com.adrian.diptrack.domain.usecase.GetBusStopDetailUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusIdUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetDestinationBusStopIdUseCase
import com.adrian.diptrack.domain.usecase.GetDestinationBusStopLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetOriginBusStopIdUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.ObserveBusArrivalInfoUseCase
import com.adrian.diptrack.domain.usecase.ObserveBusLocationUseCase
import com.adrian.diptrack.domain.usecase.ObserveCurrentModeUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
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

class MainViewModel(
    private val cancelTrackingUseCase: CancelTrackingUseCase,
    private val checkTrackingStatusUseCase: CheckTrackingStatusUseCase,
    private val getBusDetailUseCase: GetBusDetailUseCase,
    private val getBusStopDetailUseCase: GetBusStopDetailUseCase,
    private val getCurrentBusIdUseCase: GetCurrentBusIdUseCase,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getDestinationBusStopIdUseCase: GetDestinationBusStopIdUseCase,
    private val getDestinationBusStopLocationUseCase: GetDestinationBusStopLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val getOriginBusStopIdUseCase: GetOriginBusStopIdUseCase,
    private val getStaticDataUseCase: GetStaticDataUseCase,
    private val observeBusArrivalInfoUseCase: ObserveBusArrivalInfoUseCase,
    private val observeBusLocationUseCase: ObserveBusLocationUseCase,
    private val observeCurrentModeUseCase: ObserveCurrentModeUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val networkStatusObserver: NetworkStatusObserver
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
        networkStatusObserver.isConnected.collect {
            triggerInitialData()
        }
    }

    private fun observeCurrentMode() = intent {
        observeCurrentModeUseCase().collect { mode ->
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
                    is ModeState.Idle, ModeState.Arrive -> observeBusLocationUseCase(null)
                    is ModeState.Wait -> observeBusLocationUseCase(mode.busId)
                    is ModeState.Drive -> observeBusLocationUseCase(mode.busId)
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
                val busId = getCurrentBusIdUseCase()
                val originId = getOriginBusStopIdUseCase()
                val destinationId = getDestinationBusStopIdUseCase()

                when (mode) {
                    is ModeState.Idle, ModeState.Arrive -> observeBusArrivalInfoUseCase(0, 0)
                    is ModeState.Wait -> observeBusArrivalInfoUseCase(busId, originId)
                    is ModeState.Drive -> observeBusArrivalInfoUseCase(busId, destinationId)
                }
            }
            .collect { busArrivalInfo ->
                busArrivalInfo?.let { busArrivalInfo ->
                    val result = checkTrackingStatusUseCase(
                        busArrivalInfo.location,
                        busArrivalInfo.seatAvailable,
                        busArrivalInfo.etaMinutes
                    )

                    when (result) {
                        is CheckTrackingStatusResult.LocationPermissionDenied -> {
                            postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
                        }
                        is CheckTrackingStatusResult.BusFull -> {
                            startTracking(getDestinationBusStopLocationUseCase())
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
            is ModeState.Idle, ModeState.Arrive -> getStaticDataUseCase()
            else -> getFilteredBusStopsUseCase(busStopIds)
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
        when (val result = getCurrentUserLocationUseCase()) {
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
        val busDetail = getBusDetailUseCase(busId)
        reduce { state.copy(busState = busDetail.toState()) }
        postSideEffect(MainSideEffect.ShowBusDetailDialog)
    }

    private fun getBusStopDetail(busStopId: Int) = intent {
        when (val result = getBusStopDetailUseCase(busStopId)) {
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

        when (val result = startTrackingUseCase(destinationLocation)) {
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

        val busStops = cancelTrackingUseCase()

        reduce {
            state.copy(
                filteredBusStops = busStops,
                isLoading = false
            )
        }
        animateToUserLocation()
    }
}