package com.adrian.diptrack.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrian.diptrack.data.local.database.converter.RouteConverter
import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusStopEntity
import com.adrian.diptrack.data.local.database.entity.RouteEntity

@Database(
    entities = [BusStopEntity::class, RouteEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RouteConverter::class)
abstract class DipTrackDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
}