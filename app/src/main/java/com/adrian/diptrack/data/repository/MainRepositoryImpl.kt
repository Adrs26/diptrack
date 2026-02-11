package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.mapper.toBusStopDomain
import com.adrian.diptrack.data.mapper.toBusStopEntity
import com.adrian.diptrack.data.remote.dto.BusStopDto
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.BusStopResult
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow

class MainRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : MainRepository {

    override suspend fun getBusStopLocations(): BusStopResult {
        return when (val result = remoteDataSource.getBusStops()) {
            is Result.Success -> {
                val busStops = result.data + listOf(BusStopDto(
                    id = 99,
                    name = "Halte Kalideres",
                    latitude = -6.1537810,
                    longitude = 106.7084305
                ))

                localDataSource.updateBusStops(busStops.map { it.toBusStopEntity() })
                BusStopResult(
                    data = busStops.map { it.toBusStopDomain() },
                    isOffline = false,
                    error = null
                )
            }
            is Result.Error -> {
                BusStopResult(
                    data = localDataSource.getBusStops().map { it.toBusStopDomain() },
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