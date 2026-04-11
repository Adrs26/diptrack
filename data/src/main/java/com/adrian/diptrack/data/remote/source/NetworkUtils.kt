package com.adrian.diptrack.data.remote.source

interface NetworkError

enum class NetworkErrorType : NetworkError {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN
}

sealed interface Result<out D, out E: NetworkError> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: NetworkError>(val error: E): Result<Nothing, E>
}