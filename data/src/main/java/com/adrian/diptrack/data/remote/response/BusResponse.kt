package com.adrian.diptrack.data.remote.response

import com.google.gson.annotations.SerializedName

data class BusResponse(
    @SerializedName("id_bus")
    val id: Int,

    @SerializedName("plat_nomor")
    val licensePlate: String,

    @SerializedName("kode_bus")
    val busCode: String,

    @SerializedName("jenis_bus")
    val busType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("kapasitas")
    val capacity: Int,

    @SerializedName("nama_driver")
    val driver: String
)
