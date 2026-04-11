package com.adrian.diptrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus")
data class BusEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "license_plate")
    val licensePlate: String,

    @ColumnInfo(name = "bus_code")
    val busCode: String,

    @ColumnInfo(name = "max_capacity")
    val maxCapacity: Int,

    @ColumnInfo(name = "bus_type")
    val busType: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "driver")
    val driver: String,

    @ColumnInfo(name = "prev_latitude")
    val prevLatitude: Double,

    @ColumnInfo(name = "prev_longitude")
    val prevLongitude: Double,

    @ColumnInfo(name = "curr_latitude")
    val currLatitude: Double,

    @ColumnInfo(name = "curr_longitude")
    val currLongitude: Double,

    @ColumnInfo(name = "passenger")
    val passenger: Int,

    @ColumnInfo(name = "next_bus_stop_name")
    val nextBusStopName: String
)
