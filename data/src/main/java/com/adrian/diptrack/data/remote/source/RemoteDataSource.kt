package com.adrian.diptrack.data.remote.source

import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.response.RouteResponse
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

    suspend fun getBusStops(): Result<List<BusStopResponse>, NetworkError> {
        return safeApiCall { client.get("halte") }
    }

    suspend fun getRoutes(): Result<List<RouteResponse>, NetworkError> {
        return safeApiCall { client.get("jalur") }
    }

    private suspend inline fun <reified T> safeApiCall(
        crossinline apiCall: suspend () -> HttpResponse
    ): Result<T, NetworkErrorType> {
        return try {
            val response = apiCall()

            when (response.status.value) {
                in HTTP_SUCCESS -> {
                    val body = response.body<T>()
                    Result.Success(body)
                }
                HTTP_UNAUTHORIZED -> Result.Error(NetworkErrorType.UNAUTHORIZED)
                HTTP_REQUEST_TIMEOUT -> Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
                HTTP_CONFLICT -> Result.Error(NetworkErrorType.CONFLICT)
                HTTP_PAYLOAD_TOO_LARGE -> Result.Error(NetworkErrorType.PAYLOAD_TOO_LARGE)
                in HTTP_SERVER_ERROR -> Result.Error(NetworkErrorType.SERVER_ERROR)
                else -> Result.Error(NetworkErrorType.UNKNOWN)
            }
        } catch (_: SerializationException) {
            Result.Error(NetworkErrorType.SERIALIZATION)
        } catch (_: SocketTimeoutException) {
            Result.Error(NetworkErrorType.REQUEST_TIMEOUT)
        } catch (_: Exception) {
            Result.Error(NetworkErrorType.NO_INTERNET)
        }
    }

    companion object {
        private val HTTP_SUCCESS = 200..299
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_REQUEST_TIMEOUT = 408
        private const val HTTP_CONFLICT = 409
        private const val HTTP_PAYLOAD_TOO_LARGE = 413
        private val HTTP_SERVER_ERROR = 500..599
    }
}
