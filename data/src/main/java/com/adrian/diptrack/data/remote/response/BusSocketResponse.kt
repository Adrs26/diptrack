package com.adrian.diptrack.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusSocketResponse(

    @SerialName("bus_id")
    val id: Int,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("passenger_count")
    val passenger: Int,

    @SerialName("nama_halte_tujuan")
    val nextBusStopName: String,

    @SerialName("daftar_eta")
    val busStopEtas: List<BusStopEtaResponse>
)
