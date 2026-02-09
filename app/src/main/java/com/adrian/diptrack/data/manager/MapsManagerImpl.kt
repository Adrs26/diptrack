package com.adrian.diptrack.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class MapsManagerImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : MapsManager {

    override fun getUserCurrentLocation(onResult: (Coordinate?) -> Unit) {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(Coordinate(location.latitude, location.longitude))
            } else {
                onResult(null)
            }
        }
    }

    override fun calculateHaversine(
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

    override fun getLastKnownLocation(): Coordinate? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val task: Task<Location> = fusedLocationClient.lastLocation
        return try {
            val location = Tasks.await(task)
            Coordinate(location.latitude, location.longitude)
        } catch (_: Exception) {
            null
        }
    }
}