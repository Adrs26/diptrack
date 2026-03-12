package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingResult
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class MainViewModel(
    private val getCurrentBusStopUseCase: GetCurrentBusStopUseCase,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val getStaticDataUseCase: GetStaticDataUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val networkStatusObserver: NetworkStatusObserver
) : ViewModel(), ContainerHost<MainState, MainSideEffect> {

    override val container = container<MainState, MainSideEffect>(MainState())

    private var originLocation: Coordinate = Coordinate(0.0, 0.0)
    private var destinationLocation: Coordinate = Coordinate(0.0, 0.0)

    init {
        observeNetwork()
        observeCurrentBusStop()
        getInitialData()
        animateToUserLocation()
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.AnimateToUserLocation -> animateToUserLocation()
            is MainAction.RefreshData -> getInitialData()
            is MainAction.StartTracking -> startTracking(action.destinationName, action.destinationLocation)
            is MainAction.CancelTracking -> cancelTracking()
        }
    }

    private fun observeNetwork() = intent {
        networkStatusObserver.isConnected.collect {
            getInitialData()
        }
    }

    private fun observeCurrentBusStop() = intent {
        getCurrentBusStopUseCase().collect { pair ->
            reduce { state.copy(originName = pair.first) }
            originLocation = pair.second
        }
    }

    private fun getInitialData() = intent {
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
    }

    private fun animateToUserLocation() = intent {
        val coordinate = getCurrentUserLocationUseCase()
        val cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), 16f)

        postSideEffect(MainSideEffect.AnimateCamera(cameraUpdate))
    }

    private fun startTracking(destinationName: String, destinationLocation: Coordinate) = intent {
        reduce { state.copy(isLoading = true, error = null) }

        this@MainViewModel.destinationLocation = destinationLocation

        when (val result = startTrackingUseCase(originLocation, destinationLocation)) {
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
                        distance = result.distance,
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