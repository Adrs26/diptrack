package com.adrian.diptrack.presentation.screen.util

import com.adrian.diptrack.domain.model.BusArrivalInfo
import com.adrian.diptrack.domain.model.BusDetail
import com.adrian.diptrack.domain.model.BusStopDetail
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.presentation.screen.BusArrivalInfoState
import com.adrian.diptrack.presentation.screen.BusEtaState
import com.adrian.diptrack.presentation.screen.BusState
import com.adrian.diptrack.presentation.screen.BusStopState
import com.google.android.gms.maps.model.LatLng

fun Coordinate.toLatLng() = LatLng(latitude, longitude)

fun BusStopDetail.toState() = BusStopState(
    busStopName = busStopName,
    distanceKilometers = distanceKilometers,
    busEtas = busEtas.map {
        BusEtaState(
            busCode = it.busCode,
            licensePlate = it.licensePlate,
            etaMinutes = it.etaMinutes
        )
    }
)

fun BusDetail.toState() = BusState(
    busCode = busCode,
    licensePlate = licensePlate,
    driver = driver,
    seatFilled = seatFilled,
    seatAvailable = seatAvailable,
    nextBusStopName = nextBusStopName
)

fun BusArrivalInfo.toState() = BusArrivalInfoState(
    licensePlate = licensePlate,
    seatFilled = seatFilled,
    seatAvailable = seatAvailable,
    etaMinutes = etaMinutes
)