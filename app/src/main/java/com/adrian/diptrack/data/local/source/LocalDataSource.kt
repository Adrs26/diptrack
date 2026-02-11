package com.adrian.diptrack.data.local.source

import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val preferences: DipTrackPreferences,
    private val dao: MainDao
) {

    fun getGeofenceStatus(): Flow<GeofenceState> {
        return preferences.geofence
    }

    fun getCurrentBusStopId(): Flow<Int> {
        return preferences.currentBusStopId
    }

    fun getCurrentBusStopLocation(): Flow<Coordinate> {
        return preferences.currentBusStopLocation
    }

    suspend fun setGeofenceStatus(geofence: GeofenceState) {
        preferences.setGeofence(geofence)
    }

    suspend fun setCurrentBusStopId(id: Int) {
        preferences.setCurrentBusStopId(id)
    }

    suspend fun setCurrentBusStopLocation(location: Coordinate) {
        preferences.setCurrentBusStopLocation(location)
    }

    suspend fun getBusStops(): List<BusStopEntity> {
        return dao.getBusStops()
    }

    suspend fun updateBusStops(busStops: List<BusStopEntity>) {
        dao.updateBusStops(busStops)
    }
}