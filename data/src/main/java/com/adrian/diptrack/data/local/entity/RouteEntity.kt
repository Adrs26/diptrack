package com.adrian.diptrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adrian.diptrack.data.common.model.Coordinate

@Entity(tableName = "route")
data class RouteEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "polyline_routes")
    val polylines: List<Coordinate>
)
