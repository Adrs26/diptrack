package com.adrian.diptrack.domain.model

data class Route(
    val id: Int,
    val status: String,
    val polylines: List<Coordinate>
)