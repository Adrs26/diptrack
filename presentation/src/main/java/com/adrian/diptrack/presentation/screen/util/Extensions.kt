package com.adrian.diptrack.presentation.screen.util

import com.adrian.diptrack.presentation.R
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.presentation.common.UiText

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

fun Double.isCloseTo(other: Double, epsilon: Double = 1e-6): Boolean {
    return kotlin.math.abs(this - other) < epsilon
}