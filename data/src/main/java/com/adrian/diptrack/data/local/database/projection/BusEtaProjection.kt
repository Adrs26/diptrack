package com.adrian.diptrack.data.local.database.projection

data class BusEtaProjection(
    val busCode: String,
    val licensePlate: String,
    val eta: Int
)