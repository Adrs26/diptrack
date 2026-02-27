package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.usecase.CheckUserWithinBusStopRadiusUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.adrian.diptrack.presentation.screen.util.isCloseTo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class MainViewModel(
    private val checkUserWithinBusStopRadiusUseCase: CheckUserWithinBusStopRadiusUseCase,
    private val getCurrentBusStopUseCase: GetCurrentBusStopUseCase,
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val getStaticDataUseCase: GetStaticDataUseCase,
    private val networkStatusObserver: NetworkStatusObserver
) : ContainerHost<MainState, MainSideEffect>, ViewModel() {

    override val container = container<MainState, MainSideEffect>(MainState())

    private var originLocation: Coordinate = Coordinate(0.0, 0.0)
    private var destinationLocation: Coordinate = Coordinate(0.0, 0.0)

    init {
        observeNetwork()
        observeCurrentBusStop()
        getInitialData()
        animateToUserLocation()
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

    fun getInitialData() = intent {
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

    fun animateToUserLocation() = intent {
        val coordinate = getCurrentUserLocationUseCase()
        val cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), 16f)

        postSideEffect(MainSideEffect.AnimateCamera(cameraUpdate))
    }

    fun startTracking(destinationName: String, destinationLocation: Coordinate) = intent {
        reduce { state.copy(isLoading = true, error = null) }

        this@MainViewModel.destinationLocation = destinationLocation

        val isWithinBusStopRadius = checkUserWithinBusStopRadiusUseCase()
        val isSameOrigin = originLocation.latitude.isCloseTo(destinationLocation.latitude) &&
                           originLocation.longitude.isCloseTo(destinationLocation.longitude)

        if (!isWithinBusStopRadius) {
            reduce { state.copy(isLoading = false) }
            postSideEffect(MainSideEffect.ShowOutsideBusStopWarning)
            return@intent
        }

        if (isSameOrigin) {
            reduce { state.copy(isLoading = false) }
            postSideEffect(MainSideEffect.ShowSameBusStopWarning)
            return@intent
        }

        val filteredStops = getFilteredBusStopsUseCase(originLocation, destinationLocation).filteredBusStops

        reduce {
            state.copy(
                currentMode = ModeState.Wait,
                filteredBusStops = filteredStops,
                destinationName = destinationName,
                isLoading = false
            )
        }
        animateToUserLocation()
    }

    fun cancelTracking() = intent {
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