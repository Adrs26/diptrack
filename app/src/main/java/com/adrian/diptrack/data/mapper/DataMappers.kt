package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.remote.dto.BusStopDto
import com.adrian.diptrack.domain.model.BusStopLocation

fun BusStopDto.toBusStopLocation(): BusStopLocation = BusStopLocation(
    id = id,
    latitude = latitude,
    longitude = longitude
)