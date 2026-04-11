package com.adrian.diptrack.data.local.projection

data class BusEtaProjection(
    val busCode: String,
    val licensePlate: String,
    val eta: Int
)