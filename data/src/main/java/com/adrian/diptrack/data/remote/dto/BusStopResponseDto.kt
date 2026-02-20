package com.adrian.diptrack.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusStopResponseDto(
    @SerialName("id_halte")
    val id: Int,

    @SerialName("nama_halte")
    val name: String,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double
)