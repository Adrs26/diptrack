package com.adrian.diptrack.data.remote.source

import com.adrian.diptrack.data.remote.response.BusSocketResponse
import dev.icerock.moko.socket.Socket
import dev.icerock.moko.socket.SocketOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SocketManager {

    private val _messages = MutableSharedFlow<BusSocketResponse>()
    val messages = _messages.asSharedFlow()

    private val socket = Socket(
        endpoint = "http://145.79.15.182:5000/",
        config = SocketOptions(
            queryParams = null,
            transport = SocketOptions.Transport.WEBSOCKET
        )
    ) {
        on("bus_location") { data ->
            CoroutineScope(Dispatchers.IO).launch {
                val json = Json { ignoreUnknownKeys = true }
                val parsed = json.decodeFromString<BusSocketResponse>(data)
                _messages.emit(parsed)
            }
        }
    }

    fun connect() = socket.connect()

    fun disconnect() = socket.disconnect()
}