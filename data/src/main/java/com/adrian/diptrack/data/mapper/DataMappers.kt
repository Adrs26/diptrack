package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.remote.dto.BusStopResponseDto
import com.adrian.diptrack.data.remote.dto.RouteResponseDto
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.Route
import kotlinx.serialization.json.Json

fun BusStopResponseDto.toBusStopDomain(): BusStop = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopResponseDto.toBusStopEntity(): BusStopEntity = BusStopEntity(
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

fun RouteResponseDto.toRouteDomain(): Route {
    val polygon = runCatching {
        Json.decodeFromString<List<List<Double>>>(polylineRoutes)
    }.getOrDefault(emptyList())

    return Route(
        id = id,
        status = status,
        polylines = polygon.mapNotNull {
            if (it.size >= 2) {
                Coordinate(
                    latitude = it[0],
                    longitude = it[1]
                )
            } else null
        }
    )
}

fun RouteResponseDto.toRouteEntity(): RouteEntity {
    val polygon = runCatching {
        Json.decodeFromString<List<List<Double>>>(polylineRoutes)
    }.getOrDefault(emptyList())

    return RouteEntity(
        id = id,
        status = status,
        polylines = polygon.mapNotNull {
            if (it.size >= 2) {
                Coordinate(
                    latitude = it[0],
                    longitude = it[1]
                )
            } else null
        }
    )
}

fun RouteEntity.toRouteDomain(): Route {
    return Route(
        id = id,
        status = status,
        polylines = polylines
    )
}