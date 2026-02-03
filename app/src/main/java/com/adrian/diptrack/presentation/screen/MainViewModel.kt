package com.adrian.diptrack.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.usecase.GetBusStopLocationsUseCase
import com.adrian.diptrack.presentation.common.UiState
import com.adrian.diptrack.presentation.receiver.NetworkStatusReceiver
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val manager: MapsManager,
    private val getBusStopLocationsUseCase: GetBusStopLocationsUseCase,
    private val networkStatusReceiver: NetworkStatusReceiver
) : ViewModel() {

    private val _cameraUpdateEvent = Channel<CameraUpdate>(Channel.BUFFERED)
    val cameraUpdateEvent = _cameraUpdateEvent.receiveAsFlow()

    private val _mainState = MutableStateFlow(MainState())
    val mainState = _mainState.asStateFlow()

    init {
        animateToUserLocation()
        getBusStopLocations()
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkStatusReceiver.isConnected.collect {
                getBusStopLocations()
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
}