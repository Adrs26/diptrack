package com.adrian.diptrack.data.remote.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("id_jalur")
    val id: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("rute_polyline")
    val polylineRoutes: String
)
