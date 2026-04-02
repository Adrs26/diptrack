package com.adrian.diptrack.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bus_stop_eta",
    foreignKeys = [
        ForeignKey(
            entity = BusEntity::class,
            parentColumns = ["id"],
            childColumns = ["bus_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BusStopEntity::class,
            parentColumns = ["id"],
            childColumns = ["bus_stop_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bus_id"),
        Index("bus_stop_id")
    ]
)
data class BusStopEtaEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "bus_id")
    val busId: Int,

    @ColumnInfo(name = "bus_stop_id")
    val busStopId: Int,

    @ColumnInfo(name = "bus_stop_name")
    val busStopName: String,

    @ColumnInfo(name = "distance")
    val distance: Int,

    @ColumnInfo(name = "eta")
    val eta: Int
)
