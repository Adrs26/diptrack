package com.adrian.diptrack.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adrian.diptrack.data.local.database.dao.MainDao
import com.adrian.diptrack.data.local.database.entity.BusStopEntity

@Database(
    entities = [BusStopEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DipTrackDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
}