package com.adrian.diptrack.domain.model

data class BusEta(
    val busCode: String,
    val licensePlate: String,
    val etaMinutes: Int
)
