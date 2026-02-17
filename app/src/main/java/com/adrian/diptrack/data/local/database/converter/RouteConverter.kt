package com.adrian.diptrack.data.local.database.converter

import androidx.room.TypeConverter
import com.adrian.diptrack.domain.model.Coordinate
import kotlinx.serialization.json.Json

class RouteConverter {

    @TypeConverter
    fun fromCoordinateList(value: List<Coordinate>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toCoordinateList(value: String?): List<Coordinate> {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(value)
        }
    }
}