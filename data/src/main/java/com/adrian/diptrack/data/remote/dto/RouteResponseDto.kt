package com.adrian.diptrack.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteResponseDto(
    @SerialName("id_jalur")
    val id: Int,

    @SerialName("status")
    val status: String,

    @SerialName("rute_polyline")
    val polylineRoutes: String
)