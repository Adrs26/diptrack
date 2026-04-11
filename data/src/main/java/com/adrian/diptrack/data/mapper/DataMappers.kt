package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.local.entity.BusEntity
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.entity.RouteEntity
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.remote.response.BusResponse
import com.adrian.diptrack.data.remote.response.BusStopEtaResponse
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.response.RouteResponse
import kotlinx.serialization.json.Json

fun BusStopResponse.toEntity() = BusStopEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun RouteResponse.toEntity(): RouteEntity {
    val polygon = runCatching { Json.decodeFromString<List<List<Double>>>(polylineRoutes) }
        .getOrDefault(emptyList())

    return RouteEntity(
        id = id,
        status = status,
        polylines = polygon.mapNotNull {
            if (it.size >= 2) Coordinate(latitude = it[0], longitude = it[1]) else null
        }
    )
}

fun BusResponse.toEntity() = BusEntity(
    id = id,
    licensePlate = licensePlate,
    busCode = busCode,
    maxCapacity = capacity,
    busType = busType,
    status = status,
    driver = driver,
    prevLatitude = 0.0,
    prevLongitude = 0.0,
    currLatitude = 0.0,
    currLongitude = 0.0,
    passenger = 0,
    nextBusStopName = ""
)

fun BusStopEtaResponse.toEntity() = BusStopEtaEntity(
    busId = 0,
    busStopId = id,
    busStopName = name,
    distance = distance,
    eta = eta ?: 0
)
