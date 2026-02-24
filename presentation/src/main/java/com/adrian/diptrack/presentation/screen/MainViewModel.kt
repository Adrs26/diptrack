package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.IsUserInsideBusStopGeofenceUseCase
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainViewModel(
    private val manager: MapsManager,
    private val repository: MainRepository,
    private val getFilteredBusStopsUseCase: GetFilteredBusStopsUseCase,
    private val isUserInsideBusStopGeofenceUseCase: IsUserInsideBusStopGeofenceUseCase,
    private val networkStatusObserver: NetworkStatusObserver
) : ViewModel() {

    private val _cameraUpdateEvent = Channel<CameraUpdate>(Channel.BUFFERED)
    val cameraUpdateEvent = _cameraUpdateEvent.receiveAsFlow()

    private val _mainState = MutableStateFlow(MainState())
    val mainState = _mainState.asStateFlow()

    private var originLocation: Coordinate = Coordinate(0.0, 0.0)
    private var destinationLocation: Coordinate = Coordinate(0.0, 0.0)

    init {
        observeNetwork()
        observePreferences()
        animateToUserLocation()
        getInitialData()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkStatusObserver.isConnected.collect {
                refreshData()
            }
        }
    }

    fun refreshData() {
        when (mainState.value.currentMode) {
            is ModeState.Idle, ModeState.Arrive -> getInitialData()
            is ModeState.Wait, ModeState.Drive -> getFilteredBusStops()
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            val busStops = repository.getInitialData().busStops

            combine(
                repository.getGeofenceStatus(),
                repository.getCurrentBusStopId(),
                repository.getCurrentBusStopLocation()
            ) { geofence, busStopId, busStopLocation ->
                val originName = busStops.firstOrNull { it.id == busStopId }?.name.orEmpty()

                Triple(geofence, originName, busStopLocation)
            }.collect { triple ->
                _mainState.update {
                    it.copy(
                        currentGeofence = triple.first,
                        originName = triple.second
                    )
                }

                originLocation = triple.third
            }
        }
    }

    private fun getInitialData() {
        _mainState.update { it.copy(initialDataResult = UiState.Loading) }

        viewModelScope.launch {
            _mainState.update {
                it.copy(initialDataResult = UiState.Success(repository.getInitialData()))
            }
        }
    }

    private fun getFilteredBusStops() {
        _mainState.update { it.copy(initialDataResult = UiState.Loading) }

        viewModelScope.launch {
            _mainState.update {
                it.copy(
                    initialDataResult = UiState.Success(
                        getFilteredBusStopsUseCase(originLocation, destinationLocation)
                    )
                )
            }
        }
    }

    fun animateToUserLocation() {
        viewModelScope.launch {
            val coordinate = manager.getCurrentUserLocation()

            val cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(LatLng(coordinate.latitude, coordinate.longitude), 16f)

            _cameraUpdateEvent.send(cameraUpdate)
        }
    }

    fun startTracking(destinationName: String, destinationLocation: Coordinate) {
        _mainState.update { it.copy(initialDataResult = UiState.Loading) }

        this.destinationLocation = destinationLocation

        viewModelScope.launch {
            val isInsideGeofence = isUserInsideBusStopGeofenceUseCase()

            if (isInsideGeofence) {
                val result = getFilteredBusStopsUseCase(originLocation, destinationLocation)
                _mainState.update {
                    it.copy(
                        currentMode = ModeState.Wait,
                        initialDataResult = UiState.Success(result),
                        destinationName = destinationName
                    )
                }
            } else {
                _mainState.update {
                    it.copy(
                        showToast = true,
                        initialDataResult = UiState.Success(repository.getInitialData())
                    )
                }
                delay(3.seconds)
                _mainState.update { it.copy(showToast = false) }
            }
        }
    }
}