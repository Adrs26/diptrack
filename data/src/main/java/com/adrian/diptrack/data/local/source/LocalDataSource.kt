package com.adrian.diptrack.data.local.source

import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.database.projection.BusLocationProjection
import com.adrian.diptrack.data.local.database.projection.BusEtaProjection
import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val preferences: DipTrackPreferences,
    private val dao: MainDao
) {

    fun getCurrentBusStopId(): Flow<Int> = preferences.currentBusStopId

    fun getCurrentBusStopLocation(): Flow<Coordinate> = preferences.currentBusStopLocation

    suspend fun setCurrentBusStopId(id: Int) = preferences.setCurrentBusStopId(id)

    suspend fun setCurrentBusStopLocation(location: Coordinate) =
        preferences.setCurrentBusStopLocation(location)

    suspend fun getBusStops(): List<BusStopEntity> = dao.getBusStops()

    suspend fun getRoutes(): List<RouteEntity> = dao.getRoutes()

    suspend fun syncInitialData(
        busStops: List<BusStopEntity>,
        routes: List<RouteEntity>,
        buses: List<BusEntity>,
    ) = dao.syncInitialData(busStops, routes, buses)

    fun getBusLocations(): Flow<List<BusLocationProjection>> = dao.getBusLocations()

    fun getBusLocationById(id: Int): Flow<BusLocationProjection> = dao.getBusLocationById(id)

    suspend fun updateBusAndEta(
        busId: Int,
        prevLat: Double,
        prevLng: Double,
        currLat: Double,
        currLng: Double,
        passenger: Int,
        nextBusStopName: String,
        etas: List<BusStopEtaEntity>
    ) = dao.updateBusAndEta(busId, prevLat, prevLng, currLat, currLng, passenger, nextBusStopName, etas)

    suspend fun getBusStopById(id: Int): BusStopEntity = dao.getBusStopById(id)

    suspend fun getBusEtaForBusStop(id: Int): List<BusEtaProjection> = dao.getBusEtaForBusStop(id)

    suspend fun getBusById(id: Int): BusEntity = dao.getBusById(id)

    suspend fun getBusStopIdByLocation(latitude: Double, longitude: Double): Int =
        dao.getBusStopIdByLocation(latitude, longitude)

    suspend fun getNearestAvailableBusId(busStopId: Int): Int? =
        dao.getNearestAvailableBusId(busStopId)
}
