package com.adrian.diptrack.presentation.screen.util

import com.adrian.diptrack.data.local.entity.BusEntity
import com.adrian.diptrack.data.local.projection.BusArrivalInfoProjection
import com.adrian.diptrack.data.common.model.BusStopDetail
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.presentation.screen.BusArrivalInfoState
import com.adrian.diptrack.presentation.screen.BusEtaState
import com.adrian.diptrack.presentation.screen.BusState
import com.adrian.diptrack.presentation.screen.BusStopState
import com.google.android.gms.maps.model.LatLng
import kotlin.math.ceil

fun Coordinate.toLatLng() = LatLng(latitude, longitude)

fun BusStopDetail.toState() = BusStopState(
    busStopName = busStopName,
    distanceKilometers = distanceKilometers,
    busEtas = busEtas.map {
        BusEtaState(
            busCode = it.busCode,
            licensePlate = it.licensePlate,
            etaMinutes = ceil(it.eta / 60.0).toInt()
        )
    }
)

fun BusEntity.toState() = BusState(
    busCode = busCode,
    licensePlate = licensePlate,
    driver = driver,
    seatFilled = passenger,
    seatAvailable = maxCapacity - passenger,
    nextBusStopName = nextBusStopName
)

fun BusArrivalInfoProjection.toState() = BusArrivalInfoState(
    licensePlate = licensePlate,
    seatFilled = passenger,
    seatAvailable = maxCapacity - passenger,
    etaMinutes = ceil(eta / 60.0).toInt()
)
