package com.adrian.diptrack.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusStopEtaResponse(

    @SerialName("halte_id")
    val id: Int,

    @SerialName("nama_halte")
    val name: String,

    @SerialName("distance_meters")
    val distance: Int,

    @SerialName("eta_seconds")
    val eta: Int?
)
