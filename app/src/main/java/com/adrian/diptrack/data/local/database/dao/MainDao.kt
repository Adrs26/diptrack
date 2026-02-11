package com.adrian.diptrack.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adrian.diptrack.data.local.database.entity.BusStopEntity

@Dao
interface MainDao {

    @Query("SELECT * FROM bus_stop")
    suspend fun getBusStops(): List<BusStopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBusStops(busStops: List<BusStopEntity>)
}