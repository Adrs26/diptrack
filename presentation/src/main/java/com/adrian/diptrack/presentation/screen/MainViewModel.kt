package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.manager.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.usecase.GetBusInformationUseCase
import com.adrian.diptrack.domain.usecase.GetBusStopInformationResult
import com.adrian.diptrack.domain.usecase.GetBusStopInformationUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetRealtimeBusLocationUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingResult
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.adrian.diptrack.presentation.screen.util.toBusState
import com.adrian.diptrack.presentation.screen.util.toBusStopState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class MainViewModel(
    private val getBusInformationUseCase: GetBusInformationUseCase,
    private val getBusStopInformationUseCase: GetBusStopInformationUseCase,
    private val getCurrentBusStopUseCase: GetCurrentBusStopUseCase,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val getRealtimeBusLocationUseCase: GetRealtimeBusLocationUseCase,
    private val getStaticDataUseCase: GetStaticDataUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val networkStatusObserver: NetworkStatusObserver,
) : ViewModel(), ContainerHost<MainState, MainSideEffect> {

    override val container = container<MainState, MainSideEffect>(MainState())

    private val currentMode = MutableStateFlow<ModeState>(ModeState.Idle)

    private var originLocation: Coordinate = Coordinate(0.0, 0.0)
    private var destinationLocation: Coordinate = Coordinate(0.0, 0.0)

    init {
        observeNetwork()
        observeCurrentBusStop()
        triggerInitialData()
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.OnAnimateToUserLocation -> animateToUserLocation()
            is MainAction.OnRefresh -> triggerInitialData()
            is MainAction.OnBusStopClicked -> getBusStopInformation(action.busStopId)
            is MainAction.OnBusClicked -> getBusInformation(action.busId)
            is MainAction.OnStartTracking -> startTracking(action.destName, action.destLocation)
            is MainAction.OnCancelTracking -> cancelTracking()
        }
    }

    private fun observeNetwork() = intent {
        networkStatusObserver.isConnected.collect {
            triggerInitialData()
        }
    }

    private fun observeCurrentBusStop() = intent {
        getCurrentBusStopUseCase().collect { pair ->
            reduce { state.copy(originName = pair.first) }
            originLocation = pair.second
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun triggerInitialData() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val result = when (state.currentMode) {
            is ModeState.Idle, ModeState.Arrive -> getStaticDataUseCase()
            else -> getFilteredBusStopsUseCase(originLocation, destinationLocation)
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

        currentMode.flatMapLatest { mode ->
            when (mode) {
                is ModeState.Idle, ModeState.Arrive -> getRealtimeBusLocationUseCase(null)
                is ModeState.Wait -> getRealtimeBusLocationUseCase(mode.busId)
                is ModeState.Drive -> getRealtimeBusLocationUseCase(mode.busId)
            }
        }.collect {
            reduce { state.copy(busLocations = it) }
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

    private fun getBusInformation(busId: Int) = intent {
        val busInformation = getBusInformationUseCase(busId)
        reduce { state.copy(busState = busInformation.toBusState()) }
        postSideEffect(MainSideEffect.ShowBusInformationDialog)
    }

    private fun getBusStopInformation(busStopId: Int) = intent {
        when (val result = getBusStopInformationUseCase(busStopId)) {
            is GetBusStopInformationResult.LocationPermissionDenied -> {
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is GetBusStopInformationResult.Success -> {
                reduce { state.copy(busStopState = result.busStopInformation.toBusStopState()) }
                postSideEffect(MainSideEffect.ShowBusStopInformationDialog)
            }
        }
    }

    private fun startTracking(destinationName: String, destinationLocation: Coordinate) = intent {
        reduce { state.copy(isLoading = true, error = null) }

        this@MainViewModel.destinationLocation = destinationLocation

        when (val result = startTrackingUseCase(destinationLocation)) {
            is StartTrackingResult.LocationPermissionDenied -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is StartTrackingResult.NotWithinBusStopRadius -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(MainSideEffect.ShowOutsideBusStopWarning)
            }
            is StartTrackingResult.SameBusStop -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(MainSideEffect.ShowSameBusStopWarning)
            }
            is StartTrackingResult.Success -> {
                if (result.busId != null) {
                    currentMode.value = ModeState.Wait(result.busId!!)
                    reduce {
                        state.copy(
                            currentMode = ModeState.Wait(result.busId!!),
                            filteredBusStops = result.filteredStops,
                            distanceKilometers = result.distance,
                            destinationName = destinationName,
                            isLoading = false
                        )
                    }
                    animateToUserLocation()
                } else {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(MainSideEffect.ShowNoAvailableBusWarning)
                }
            }
        }
    }

    private fun cancelTracking() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val busStops = getStaticDataUseCase().busStops

        reduce {
            state.copy(
                currentMode = ModeState.Idle,
                filteredBusStops = busStops,
                isLoading = false
            )
        }
        animateToUserLocation()
    }
}