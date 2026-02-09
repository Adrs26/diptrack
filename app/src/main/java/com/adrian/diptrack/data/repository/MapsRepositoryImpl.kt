package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.mapper.toBusStopDomain
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.BusStopLocationResult
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MapsRepository
import kotlinx.coroutines.flow.Flow

class MapsRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : MapsRepository {

    override suspend fun getBusStopLocations(): BusStopLocationResult {
        return when (val result = remoteDataSource.getBusStops()) {
            is Result.Success -> {
                BusStopLocationResult(
                    data = result.data.map { it.toBusStopDomain() } + listOf(BusStop(
                        id = 99,
                        name = "Halte Kalideres",
                        latitude = -6.1537810,
                        longitude = 106.7084305
                    )),
                    isOffline = false,
                    error = null
                )
            }
            is Result.Error -> {
                BusStopLocationResult(
                    data = listOf(
                        BusStop(
                            id = 1,
                            name = "Halte 1",
                            latitude = -7.05538964,
                            longitude = 110.43927992
                        ),
                        BusStop(
                            id = 2,
                            name = "Halte 2",
                            latitude = -7.05352606,
                            longitude = 110.43889375
                        )
                    ),
                    isOffline = true,
                    error = result.error
                )
            }
        }
    }

    override suspend fun getGeofenceStatus(): Flow<GeofenceState> {
        return localDataSource.getGeofenceStatus()
    }

    override suspend fun getCurrentBusStopId(): Flow<Int> {
        return localDataSource.getCurrentBusStopId()
    }

    override suspend fun getCurrentBusStopLocation(): Flow<Coordinate> {
        return localDataSource.getCurrentBusStopLocation()
    }

    override suspend fun setGeofenceStatus(geofence: GeofenceState) {
        localDataSource.setGeofenceStatus(geofence)
    }

    override suspend fun setCurrentBusStopId(id: Int) {
        localDataSource.setCurrentBusStopId(id)
    }

    override suspend fun setCurrentBusStopLocation(coordinate: Coordinate) {
        localDataSource.setCurrentBusStopLocation(coordinate)
    }
}