package com.adrian.diptrack.data.remote.source

import com.adrian.diptrack.data.remote.dto.BusStopResponseDto
import com.adrian.diptrack.data.remote.dto.RouteResponseDto
import com.adrian.diptrack.domain.common.NetworkError
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.common.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerializationException

class RemoteDataSource(private val client: HttpClient) {

    suspend fun getBusStops(): Result<List<BusStopResponseDto>, NetworkError> {
        return safeApiCall { client.get("halte") }
    }

    suspend fun getRoutes(): Result<List<RouteResponseDto>, NetworkError> {
        return safeApiCall { client.get("jalur") }
    }

    private suspend inline fun <reified T> safeApiCall(
        crossinline apiCall: suspend () -> HttpResponse
    ): Result<T, NetworkErrorType> {
        return try {
            val response = apiCall()

            when (response.status.value) {
                in 200..299 -> {
                    val body = response.body<T>()
                    Result.Success(body)
                }
                401 -> Result.Error(NetworkErrorType.UNAUTHORIZED)
                408 -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                409 -> Result.Error(NetworkErrorType.CONFLICT)
                413 -> Result.Error(NetworkErrorType.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkErrorType.SERVER_ERROR)
                else -> Result.Error(NetworkErrorType.UNKNOWN)
            }
        } catch (e: Exception) {
            when (e) {
                is SerializationException -> Result.Error(NetworkErrorType.SERIALIZATION)
                is SocketTimeoutException -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                else -> Result.Error(NetworkErrorType.NO_INTERNET)
            }
        }
    }
}