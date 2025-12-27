package com.adrian.diptrack.domain.model

data class Route(
    val id: Int,
    val polylines: List<List<Double>>
)
