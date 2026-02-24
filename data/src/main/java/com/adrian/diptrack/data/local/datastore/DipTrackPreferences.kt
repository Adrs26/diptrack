package com.adrian.diptrack.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("diptrack_prefs")

class DipTrackPreferences(private val context: Context) {

    val geofence = context.dataStore.data.map { preferences ->
        when (preferences[GEOFENCE_KEY]) {
            GeofenceState.Enter.toString() -> GeofenceState.Enter
            GeofenceState.Exit.toString() -> GeofenceState.Exit
            else -> GeofenceState.Idle
        }
    }

    val currentBusStopId = context.dataStore.data.map { preferences ->
        preferences[BUS_STOP_ID_KEY] ?: -1
    }

    val currentBusStopLocation = context.dataStore.data.map { preferences ->
        Coordinate(
            latitude = preferences[BUS_STOP_LATITUDE_KEY] ?: 0.0,
            longitude = preferences[BUS_STOP_LONGITUDE_KEY] ?: 0.0
        )
    }

    suspend fun setGeofence(geofence: GeofenceState) {
        context.dataStore.edit { preferences ->
            preferences[GEOFENCE_KEY] = geofence.toString()
        }
    }

    suspend fun setCurrentBusStopId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_ID_KEY] = id
        }
    }

    suspend fun setCurrentBusStopLocation(location: Coordinate) {
        context.dataStore.edit { preferences ->
            preferences[BUS_STOP_LATITUDE_KEY] = location.latitude
            preferences[BUS_STOP_LONGITUDE_KEY] = location.longitude
        }
    }

    companion object {
        private val GEOFENCE_KEY = stringPreferencesKey("geofence")
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
        private val BUS_STOP_LATITUDE_KEY = doublePreferencesKey("bus_stop_latitude")
        private val BUS_STOP_LONGITUDE_KEY = doublePreferencesKey("bus_stop_longitude")
    }
}
