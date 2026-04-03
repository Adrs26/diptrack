package com.adrian.diptrack.presentation.screen.util

import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.model.BusEtaInformation
import com.adrian.diptrack.domain.model.BusInformation
import com.adrian.diptrack.domain.model.BusStopInformation
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.presentation.common.UiText
import com.adrian.diptrack.presentation.screen.BusEtaState
import com.adrian.diptrack.presentation.screen.BusState
import com.adrian.diptrack.presentation.screen.BusStopState
import com.google.android.gms.maps.model.LatLng

fun NetworkError.toMessageError(): UiText {
    return when(this) {
        NetworkErrorType.REQUEST_TIMEOUT -> UiText.StringResource(R.string.request_timeout)
        NetworkErrorType.UNAUTHORIZED -> UiText.StringResource(R.string.unauthorized)
        NetworkErrorType.CONFLICT -> UiText.StringResource(R.string.conflict)
        NetworkErrorType.NO_INTERNET -> UiText.StringResource(R.string.no_internet)
        NetworkErrorType.PAYLOAD_TOO_LARGE -> UiText.StringResource(R.string.payload_too_large)
        NetworkErrorType.SERVER_ERROR -> UiText.StringResource(R.string.server_error)
        NetworkErrorType.SERIALIZATION -> UiText.StringResource(R.string.serialization_error)
        else -> UiText.StringResource(R.string.unknown_error)
    }
}

fun Coordinate.toLatLng() = LatLng(latitude, longitude)

fun BusStopInformation.toBusStopState() = BusStopState(
    busStopName = busStopName,
    distanceKilometers = distanceKilometers,
    busEtas = busEtas.map { it.toBusEtaState() }
)

private fun BusEtaInformation.toBusEtaState() = BusEtaState(
    busCode = busCode,
    numberPlate = numberPlate,
    etaMinutes = etaMinutes
)

fun BusInformation.toBusState() = BusState(
    busCode = busCode,
    numberPlate = numberPlate,
    driverName = driverName,
    currPassenger = currPassenger,
    availPassenger = availPassenger,
    nextBusStopName = nextBusStopName
)