package com.adrian.diptrack.data.local.source

import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val preferences: DipTrackPreferences
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
}