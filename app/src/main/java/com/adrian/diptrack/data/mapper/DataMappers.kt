package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.remote.dto.BusStopDto
import com.adrian.diptrack.domain.model.BusStop

fun BusStopDto.toBusStopDomain(): BusStop = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)