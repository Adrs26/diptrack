package com.adrian.diptrack.domain.manager

import com.adrian.diptrack.domain.model.Coordinate

interface MapsManager {

    fun getUserCurrentLocation(onResult: (Coordinate?) -> Unit)
}