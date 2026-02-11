package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.model.BusStopResult
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun getBusStopLocations(): BusStopResult

    suspend fun getGeofenceStatus(): Flow<GeofenceState>

    suspend fun getCurrentBusStopId(): Flow<Int>

    suspend fun getCurrentBusStopLocation(): Flow<Coordinate>

    suspend fun setGeofenceStatus(geofence: GeofenceState)

    suspend fun setCurrentBusStopId(id: Int)

    suspend fun setCurrentBusStopLocation(coordinate: Coordinate)
}