package com.adrian.diptrack.data.local.source

import androidx.room.withTransaction
import com.adrian.diptrack.data.local.database.DipTrackDatabase
import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val preferences: DipTrackPreferences,
    private val database: DipTrackDatabase,
    private val dao: MainDao
) {

    fun getCurrentBusStopId(): Flow<Int> = preferences.currentBusStopId

    fun getCurrentBusStopLocation(): Flow<Coordinate> = preferences.currentBusStopLocation

    suspend fun setCurrentBusStopId(id: Int) = preferences.setCurrentBusStopId(id)

    suspend fun setCurrentBusStopLocation(location: Coordinate) = preferences.setCurrentBusStopLocation(location)

    suspend fun getBusStops(): List<BusStopEntity> = dao.getBusStops()

    suspend fun getRoutes(): List<RouteEntity> = dao.getRoutes()

    suspend fun syncInitialData(
        busStops: List<BusStopEntity>,
        routes: List<RouteEntity>
    ) {
        database.withTransaction {
            dao.upsertBusStops(busStops)
            dao.upsertRoutes(routes)
        }
    }
}
