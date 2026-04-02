package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.local.database.entity.BusEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.database.projection.BusLocationProjection
import com.adrian.diptrack.data.local.database.projection.BusEtaProjection
import com.adrian.diptrack.data.remote.response.BusResponse
import com.adrian.diptrack.data.remote.response.BusStopEtaResponse
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.response.RouteResponse
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.Route
import kotlinx.serialization.json.Json

fun BusStopResponse.toBusStopDomain(): BusStop = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopResponse.toBusStopEntity(): BusStopEntity = BusStopEntity(
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

fun RouteResponse.toRouteDomain(): Route {
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

fun RouteResponse.toRouteEntity(): RouteEntity {
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

fun BusResponse.toBusEntity() = BusEntity(
    id = id,
    numberPlate = numberPlate,
    busCode = busCode,
    maxCapacity = capacity,
    busType = busType,
    status = status,
    driverName = driverName,
    prevLatitude = 0.0,
    prevLongitude = 0.0,
    currLatitude = 0.0,
    currLongitude = 0.0,
    passenger = 0,
    nextBusStopName = ""
)

fun BusLocationProjection.toBusLocationDomain() = BusLocation(
    id = id,
    previousLocation = Coordinate(
        latitude = prevLatitude,
        longitude = prevLongitude
    ),
    currentLocation = Coordinate(
        latitude = currLatitude,
        longitude = currLongitude
    )
)

fun BusStopEtaResponse.toBusStopEtaEntity() = BusStopEtaEntity(
    busId = 0,
    busStopId = id,
    busStopName = name,
    distance = distance,
    eta = eta ?: 0
)

fun BusEtaProjection.toBusEtaDomain() = BusEta(
    busCode = busCode,
    numberPlate = numberPlate,
    eta = eta
)