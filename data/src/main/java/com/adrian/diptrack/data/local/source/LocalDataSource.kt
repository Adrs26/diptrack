package com.adrian.diptrack.data.local.source

import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.database.projection.BusArrivalInfoProjection
import com.adrian.diptrack.data.local.database.projection.BusEtaProjection
import com.adrian.diptrack.data.local.database.projection.BusLocationProjection
import kotlinx.coroutines.flow.Flow

class LocalDataSource(private val dao: MainDao) {

    suspend fun getBusStops(): List<BusStopEntity> = dao.getBusStops()

    suspend fun getRoutes(): List<RouteEntity> = dao.getRoutes()

    suspend fun syncInitialData(
        busStops: List<BusStopEntity>,
        routes: List<RouteEntity>,
        buses: List<BusEntity>,
    ) = dao.syncInitialData(busStops, routes, buses)

    fun getActiveBusLocations(): Flow<List<BusLocationProjection>> = dao.getActiveBusLocations()

    fun getActiveBusLocationById(id: Int): Flow<BusLocationProjection> = dao.getActiveBusLocationById(id)

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

    fun getNearestAvailableBus(busStopId: Int): Flow<List<Int>> = dao.getNearestAvailableBus(busStopId)

    fun getAllAvailableBusId(busStopId: Int): Flow<List<Int>> = dao.getAllAvailableBusId(busStopId)

    fun observeBusArrivalInfo(busId: Int, busStopId: Int): Flow<BusArrivalInfoProjection?> =
        dao.observeBusArrivalInfo(busId, busStopId)

}
