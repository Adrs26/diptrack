package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.repository.MapsRepository
import com.adrian.diptrack.domain.usecase.GetBusStopLocationsUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentGeofenceStatusUseCase
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val manager: MapsManager,
    private val repository: MapsRepository,
    private val getBusStopLocationsUseCase: GetBusStopLocationsUseCase,
    private val getCurrentGeofenceStatusUseCase: GetCurrentGeofenceStatusUseCase,
    private val networkStatusObserver: NetworkStatusObserver
) : ViewModel() {

    private val _cameraUpdateEvent = Channel<CameraUpdate>(Channel.BUFFERED)
    val cameraUpdateEvent = _cameraUpdateEvent.receiveAsFlow()

    private val _mainState = MutableStateFlow(MainState())
    val mainState = _mainState.asStateFlow()

//    private var busStopOriginLocation = LatLng(0.0, 0.0)
//    private var busStopDestinationLocation = LatLng(0.0, 0.0)

    init {
        observeNetwork()
        observePreferences()
        animateToUserLocation()
        getBusStopLocations()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkStatusObserver.isConnected.collect {
                getBusStopLocations()
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                repository.getGeofenceStatus(),
                repository.getCurrentBusStopId()
            ) { geofence, busStopId ->
                Pair(geofence, busStopId)
            }.collect { pair ->
                val originName = getBusStopLocationsUseCase().data
                    .firstOrNull { it.id == pair.second }?.name ?: ""

                _mainState.update {
                    it.copy(
                        currentGeofence = pair.first,
                        originName = originName
                    )
                }
            }
        }
    }

    fun getCurrentGeofenceStatus(isGranted: Boolean, isMapLoaded: Boolean) {
        if (isGranted && isMapLoaded) {
            viewModelScope.launch {
                getCurrentGeofenceStatusUseCase()
            }
        }
    }

    fun animateToUserLocation() {
        manager.getUserCurrentLocation{ coordinate ->
            coordinate?.let {
                viewModelScope.launch {
                    val cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                    _cameraUpdateEvent.send(cameraUpdate)
                }
            }
        }
    }

    fun getBusStopLocations() {
        _mainState.update {
            it.copy(
                busStopLocations = UiState.Loading
            )
        }

        viewModelScope.launch {
            _mainState.update {
                it.copy(
                    busStopLocations = UiState.Success(getBusStopLocationsUseCase())
                )
            }
        }
    }
}