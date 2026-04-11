package com.adrian.diptrack.data.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double
)
