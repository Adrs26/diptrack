package com.adrian.diptrack.data.remote.response

import com.google.gson.annotations.SerializedName

data class BusStopResponse(
    @SerializedName("id_halte")
    val id: Int,

    @SerializedName("nama_halte")
    val name: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)

