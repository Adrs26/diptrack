package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.mapper.toBusStopLocation
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.BusStopLocationResult
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.BusStopLocation
import com.adrian.diptrack.domain.repository.MapsRepository

class MapsRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : MapsRepository {

    override suspend fun getBusStopLocations(): BusStopLocationResult {
        return when (val result = remoteDataSource.getBusStops()) {
            is Result.Success -> {
                BusStopLocationResult(
                    data = result.data.map { it.toBusStopLocation() },
                    isOffline = false,
                    error = null
                )
            }
            is Result.Error -> {
                BusStopLocationResult(
                    data = listOf(
                        BusStopLocation(
                            id = 1,
                            latitude = -7.05538964,
                            longitude = 110.43927992
                        ),
                        BusStopLocation(
                            id = 2,
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
}