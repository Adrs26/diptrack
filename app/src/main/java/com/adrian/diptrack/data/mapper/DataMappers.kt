package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.remote.dto.BusStopDto
import com.adrian.diptrack.domain.model.BusStop

fun BusStopDto.toBusStopDomain(): BusStop = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopDto.toBusStopEntity(): BusStopEntity = BusStopEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopEntity.toBusStopDomain(): BusStop = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)
