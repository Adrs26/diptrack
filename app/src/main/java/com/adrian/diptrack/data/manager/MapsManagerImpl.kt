package com.adrian.diptrack.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.model.Coordinate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient

class MapsManagerImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geofencingClient: GeofencingClient
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
}