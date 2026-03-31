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

    @SerialName("daftar_eta")
    val etaBusStops: List<EtaBusStopsResponse>
)

@Serializable
data class EtaBusStopsResponse(

    @SerialName("halte_id")
    val id: Int,

    @SerialName("nama_halte")
    val name: String,

    @SerialName("distance_meters")
    val distance: Int,

    @SerialName("eta_seconds")
    val eta: Int?
)