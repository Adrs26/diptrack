package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.manager.MapsPermissionResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetBusStopInformationResult
import com.adrian.diptrack.domain.usecase.GetBusStopInformationUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingResult
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.adrian.diptrack.presentation.screen.util.toBusStopState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class MainViewModel(
    private val getBusStopInformationUseCase: GetBusStopInformationUseCase,
    private val getCurrentBusStopUseCase: GetCurrentBusStopUseCase,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val getStaticDataUseCase: GetStaticDataUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val networkStatusObserver: NetworkStatusObserver,
    private val repository: MainRepository,
) : ViewModel(), ContainerHost<MainState, MainSideEffect> {

    override val container = container<MainState, MainSideEffect>(MainState())

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

        repository.getRealtimeBusData().collect { bus ->
            reduce { state.copy(busLocations = bus) }
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

    private fun getBusStopInformation(busStopId: Int) = intent {
        when (val result = getBusStopInformationUseCase(busStopId)) {
            is GetBusStopInformationResult.LocationPermissionDenied -> {
                postSideEffect(MainSideEffect.ShowLocationPermissionDeniedWarning)
            }
            is GetBusStopInformationResult.Success -> {
                result.busStopInformation.collect { busStopInformation ->
                    reduce { state.copy(busStopState = busStopInformation.toBusStopState()) }
                    postSideEffect(MainSideEffect.ShowBusStopInformationDialog)
                }
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
                reduce {
                    state.copy(
                        currentMode = ModeState.Wait,
                        filteredBusStops = result.filteredStops,
                        distanceKilometers = result.distance,
                        destinationName = destinationName,
                        isLoading = false
                    )
                }
                animateToUserLocation()
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