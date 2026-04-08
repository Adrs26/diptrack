package com.adrian.diptrack.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.adrian.diptrack.data.local.database.entity.BusEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity
import com.adrian.diptrack.data.local.database.projection.BusEtaProjection
import com.adrian.diptrack.data.local.database.projection.BusLocationProjection
import com.adrian.diptrack.data.local.database.projection.BusArrivalInfoProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface MainDao {

    @Query("SELECT * FROM bus_stop")
    suspend fun getBusStops(): List<BusStopEntity>

    @Query("SELECT * FROM route")
    suspend fun getRoutes(): List<RouteEntity>

    @Transaction
    suspend fun syncInitialData(
        busStops: List<BusStopEntity>,
        routes: List<RouteEntity>,
        buses: List<BusEntity>,
    ) {
        upsertBusStops(busStops)
        upsertRoutes(routes)
        upsertBuses(buses)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBusStops(busStops: List<BusStopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuses(buses: List<BusEntity>)

    @Query("""
        SELECT 
            id,
            prev_latitude AS prevLatitude,
            prev_longitude AS prevLongitude,
            curr_latitude AS currLatitude,
            curr_longitude AS currLongitude
        FROM bus WHERE status = 'berjalan'
    """)
    fun getActiveBusLocations(): Flow<List<BusLocationProjection>>

    @Query("""
        SELECT 
            id,
            prev_latitude AS prevLatitude,
            prev_longitude AS prevLongitude,
            curr_latitude AS currLatitude,
            curr_longitude AS currLongitude
        FROM bus WHERE id = :id AND status = 'berjalan'
    """)
    fun getActiveBusLocationById(id: Int): Flow<BusLocationProjection>

    @Transaction
    suspend fun updateBusAndEta(
        busId: Int,
        prevLat: Double,
        prevLng: Double,
        currLat: Double,
        currLng: Double,
        passenger: Int,
        nextBusStopName: String,
        etas: List<BusStopEtaEntity>
    ) {
        updateBusLocationAndPassenger(busId, prevLat, prevLng, currLat, currLng, passenger, nextBusStopName)
        deleteEtaByBusId(busId)
        insertEta(etas.map { it.copy(busId = busId) })
    }

    @Query("""
        UPDATE bus 
        SET prev_latitude = :prevLat, 
            prev_longitude = :prevLng,
            curr_latitude = :currLat,
            curr_longitude = :currLng,
            passenger = :passenger,
            next_bus_stop_name = :nextBusStopName
        WHERE id = :id
    """)
    suspend fun updateBusLocationAndPassenger(
        id: Int,
        prevLat: Double,
        prevLng: Double,
        currLat: Double,
        currLng: Double,
        passenger: Int,
        nextBusStopName: String
    )

    @Query("DELETE FROM bus_stop_eta WHERE bus_id = :busId")
    suspend fun deleteEtaByBusId(busId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEta(list: List<BusStopEtaEntity>)

    @Query("SELECT * FROM bus_stop WHERE id = :id")
    suspend fun getBusStopById(id: Int): BusStopEntity

    @Query("""
        SELECT 
            bus.bus_code AS busCode,
            bus.license_plate AS licensePlate,
            eta.eta AS eta
        FROM bus
        INNER JOIN bus_stop_eta AS eta
        ON eta.bus_id = bus.id AND eta.bus_stop_id = :busStopId ORDER BY eta.eta ASC
    """)
    suspend fun getBusEtaForBusStop(busStopId: Int): List<BusEtaProjection>

    @Query("SELECT * FROM bus WHERE id = :id")
    suspend fun getBusById(id: Int): BusEntity

    @Query("""
        SELECT b.id FROM bus b
        JOIN bus_stop_eta bse ON b.id = bse.bus_id
        WHERE bse.bus_stop_id = :busStopId
          AND b.passenger < b.max_capacity
          AND b.status = 'berjalan'
        ORDER BY bse.eta ASC
    """)
    fun getNearestAvailableBus(busStopId: Int): Flow<List<Int>>

    @Query("""
        SELECT bus_id
        FROM bus_stop_eta
        WHERE bus_stop_id = :busStopId
        ORDER BY eta ASC
    """)
    fun getAllAvailableBusId(busStopId: Int): Flow<List<Int>>

    @Query("""
        SELECT 
            b.bus_code AS busCode,
            b.curr_latitude AS latitude,
            b.curr_longitude AS longitude,
            b.license_plate AS licensePlate,
            b.max_capacity AS maxCapacity, 
            b.passenger AS passenger,
            bse.bus_stop_name AS busStopName,
            bse.eta AS eta
        FROM bus_stop_eta bse
        JOIN bus b ON b.id = bse.bus_id
        WHERE b.id = :busId AND bse.bus_stop_id = :busStopId
    """)
    fun observeBusArrivalInfo(busId: Int, busStopId: Int): Flow<BusArrivalInfoProjection?>
}
