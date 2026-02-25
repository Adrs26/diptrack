package com.adrian.diptrack.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.map

class DipTrackPreferences(private val dataStore: DataStore<Preferences>) {

    val currentBusStopId = dataStore.data.map { preferences ->
        preferences[BUS_STOP_ID_KEY] ?: -1
    }

    val currentBusStopLocation = dataStore.data.map { preferences ->
        Coordinate(
            latitude = preferences[BUS_STOP_LATITUDE_KEY] ?: 0.0,
            longitude = preferences[BUS_STOP_LONGITUDE_KEY] ?: 0.0
        )
    }

    suspend fun setCurrentBusStopId(id: Int) {
        dataStore.edit { preferences ->
            preferences[BUS_STOP_ID_KEY] = id
        }
    }

    suspend fun setCurrentBusStopLocation(location: Coordinate) {
        dataStore.edit { preferences ->
            preferences[BUS_STOP_LATITUDE_KEY] = location.latitude
            preferences[BUS_STOP_LONGITUDE_KEY] = location.longitude
        }
    }

    private companion object {
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
        private val BUS_STOP_LATITUDE_KEY = doublePreferencesKey("bus_stop_latitude")
        private val BUS_STOP_LONGITUDE_KEY = doublePreferencesKey("bus_stop_longitude")
    }
}
