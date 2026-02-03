package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.common.BusStopLocationResult

interface MapsRepository {

    suspend fun getBusStopLocations(): BusStopLocationResult
}