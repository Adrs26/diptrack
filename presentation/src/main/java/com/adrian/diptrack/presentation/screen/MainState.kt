package com.adrian.diptrack.presentation.screen

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Route

data class MainState(
    val currentMode: ModeState = ModeState.Idle,
    val busStops: List<BusStop> = emptyList(),
    val filteredBusStops: List<BusStop> = emptyList(),
    val busStopState: BusStopState = BusStopState(),
    val routes: List<Route> = emptyList(),
    val busLocations: List<BusLocation> = emptyList(),
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
