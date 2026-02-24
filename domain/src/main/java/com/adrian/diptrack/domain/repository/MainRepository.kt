package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.InitialDataResult
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun getInitialData(): InitialDataResult

    fun getGeofenceStatus(): Flow<GeofenceState>

    fun getCurrentBusStopId(): Flow<Int>

    fun getCurrentBusStopLocation(): Flow<Coordinate>

    suspend fun setGeofenceStatus(geofence: GeofenceState)

    suspend fun setCurrentBusStopId(id: Int)

    suspend fun setCurrentBusStopLocation(coordinate: Coordinate)
}
