package com.adrian.diptrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrian.diptrack.data.local.converter.RouteConverter
import com.adrian.diptrack.data.local.dao.MainDao
import com.adrian.diptrack.data.local.entity.BusEntity
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.entity.BusStopEtaEntity
import com.adrian.diptrack.data.local.entity.RouteEntity

@Database(
    entities = [
        BusEntity::class,
        BusStopEntity::class,
        BusStopEtaEntity::class,
        RouteEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(RouteConverter::class)
abstract class DipTrackDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
}