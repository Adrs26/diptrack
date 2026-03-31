package com.adrian.diptrack.domain.model

data class BusSocket(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val passenger: Int,
    val etaBusStops: List<EtaBusStops>
)

data class EtaBusStops(
    val id: Int,
    val name: String,
    val distance: Int,
    val eta: Int?
)
