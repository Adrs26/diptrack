package com.adrian.diptrack.data.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.common.model.Coordinate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapsManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentUserLocation(): MapsPermissionResult {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return MapsPermissionResult.Denied
        }

        return try {
            val location = fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()

            if (location != null) {
                MapsPermissionResult.Granted(Coordinate(location.latitude, location.longitude))
            } else {
                MapsPermissionResult.Denied
            }
        } catch (_: Exception) {
            MapsPermissionResult.Denied
        }
    }

    fun calculateHaversine(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            startCoordinate.latitude,
            startCoordinate.longitude,
            endCoordinate.latitude,
            endCoordinate.longitude,
            results
        )
        return results[0]
    }

    fun calculateDistanceInKm(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(endCoordinate.latitude - startCoordinate.latitude)
        val dLon = Math.toRadians(endCoordinate.longitude - startCoordinate.longitude)

        val rLat1 = Math.toRadians(startCoordinate.latitude)
        val rLat2 = Math.toRadians(endCoordinate.latitude)

        val a = sin(dLat / 2).pow(2) + cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
