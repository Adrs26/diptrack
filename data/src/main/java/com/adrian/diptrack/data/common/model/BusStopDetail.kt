package com.adrian.diptrack.data.common.model

import com.adrian.diptrack.data.local.projection.BusEtaProjection

data class BusStopDetail(
    val busStopName: String,
    val distanceKilometers: Double,
    val busEtas: List<BusEtaProjection>
)
