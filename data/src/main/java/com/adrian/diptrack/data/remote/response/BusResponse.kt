package com.adrian.diptrack.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusResponse(
    @SerialName("id_bus")
    val id: Int,

    @SerialName("plat_nomor")
    val licensePlate: String,

    @SerialName("kode_bus")
    val busCode: String,

    @SerialName("jenis_bus")
    val busType: String,

    @SerialName("status")
    val status: String,

    @SerialName("kapasitas")
    val capacity: Int,

    @SerialName("nama_driver")
    val driver: String
)
