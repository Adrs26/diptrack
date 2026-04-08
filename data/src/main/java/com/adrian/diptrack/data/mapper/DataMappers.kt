package com.adrian.diptrack.data.mapper

import com.adrian.diptrack.data.local.database.entity.BusEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.database.projection.BusArrivalInfoProjection
import com.adrian.diptrack.data.local.database.projection.BusEtaProjection
import com.adrian.diptrack.data.local.database.projection.BusLocationProjection
import com.adrian.diptrack.data.remote.response.BusResponse
import com.adrian.diptrack.data.remote.response.BusStopEtaResponse
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.response.RouteResponse
import com.adrian.diptrack.domain.model.BusArrivalInfo
import com.adrian.diptrack.domain.model.BusDetail
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.Route
import kotlinx.serialization.json.Json
import kotlin.math.ceil

fun BusStopResponse.toDomain() = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopResponse.toEntity() = BusStopEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun BusStopEntity.toDomain() = BusStop(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude
)

fun RouteResponse.toDomain(): Route {
    val polygon = runCatching { Json.decodeFromString<List<List<Double>>>(polylineRoutes) }
        .getOrDefault(emptyList())

    return Route(
        id = id,
        status = status,
        polylines = polygon.mapNotNull {
            if (it.size >= 2) Coordinate(latitude = it[0], longitude = it[1]) else null
        }
    )
}

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

fun RouteEntity.toDomain() = Route(
    id = id,
    status = status,
    polylines = polylines
)

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

fun BusLocationProjection.toDomain() = BusLocation(
    id = id,
    previousLocation = Coordinate(prevLatitude, prevLongitude),
    currentLocation = Coordinate(currLatitude, currLongitude)
)

fun BusStopEtaResponse.toEntity() = BusStopEtaEntity(
    busId = 0,
    busStopId = id,
    busStopName = name,
    distance = distance,
    eta = eta ?: 0
)

fun BusEtaProjection.toDomain() = BusEta(
    busCode = busCode,
    licensePlate = licensePlate,
    etaMinutes = ceil(eta / 60.0).toInt()
)

fun BusEntity.toDomain() = BusDetail(
    busCode = busCode,
    licensePlate = licensePlate,
    driver = driver,
    seatFilled = passenger,
    seatAvailable = maxCapacity - passenger,
    nextBusStopName = nextBusStopName
)

fun BusArrivalInfoProjection.toDomain() = BusArrivalInfo(
    busCode = busCode,
    location = Coordinate(latitude, longitude),
    licensePlate = licensePlate,
    seatFilled = passenger,
    seatAvailable = maxCapacity - passenger,
    busStopName = busStopName,
    etaMinutes = ceil(eta / 60.0).toInt()
)
