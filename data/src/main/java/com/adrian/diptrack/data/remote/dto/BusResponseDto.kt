package com.adrian.diptrack.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusResponseDto(
    @SerialName("id_bus")
    val id: Int,

    @SerialName("plat_nomor")
    val numberPlate: String,

    @SerialName("kode_bus")
    val busCode: String,

    @SerialName("jenis_bus")
    val busType: String,

    @SerialName("status")
    val status: String,

    @SerialName("kapasitas")
    val capacity: Int,

    @SerialName("penumpang")
    val passenger: Int,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("next_halte_id")
    val nextBusStopId: Int,

    @SerialName("nama_driver")
    val driverName: String
)
