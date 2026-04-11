package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.data.common.ModeState
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.entity.RouteEntity
import com.adrian.diptrack.data.local.projection.BusLocationProjection
import com.adrian.diptrack.data.remote.source.NetworkError

data class MainState(
    val currentMode: ModeState = ModeState.Idle,
    val busStops: List<BusStopEntity> = emptyList(),
    val filteredBusStops: List<BusStopEntity> = emptyList(),
    val busStopState: BusStopState = BusStopState(),
    val routes: List<RouteEntity> = emptyList(),
    val busLocations: List<BusLocationProjection> = emptyList(),
    val busState: BusState = BusState(),
    val busArrivalInfoState: BusArrivalInfoState = BusArrivalInfoState(),
    val distanceKilometers: Double = 0.0,
    val originName: String = "",
    val destinationName: String = "",
    val isLoading: Boolean = false,
    val error: NetworkError? = null
)

data class BusStopState(
    val busStopName: String = "",
    val distanceKilometers: Double = 0.0,
    val busEtas: List<BusEtaState> = emptyList()
)

data class BusEtaState(
    val busCode: String = "",
    val licensePlate: String = "",
    val etaMinutes: Int = 0
)

data class BusState(
    val busCode: String = "",
    val licensePlate: String = "",
    val driver: String = "",
    val seatFilled: Int = 0,
    val seatAvailable: Int = 0,
    val nextBusStopName: String = ""
)

data class BusArrivalInfoState(
    val licensePlate: String = "",
    val seatFilled: Int = 0,
    val seatAvailable: Int = 0,
    val etaMinutes: Int = 0
)
