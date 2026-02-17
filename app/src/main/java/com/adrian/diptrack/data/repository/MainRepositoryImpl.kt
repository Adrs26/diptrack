package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.mapper.toBusStopDomain
import com.adrian.diptrack.data.mapper.toBusStopEntity
import com.adrian.diptrack.data.mapper.toRouteDomain
import com.adrian.diptrack.data.mapper.toRouteEntity
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.GeofenceState
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.InitialDataResult
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class MainRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : MainRepository {

    override suspend fun getInitialData(): InitialDataResult = coroutineScope {
        val busStopDeferred = async { remoteDataSource.getBusStops() }
        val routeDeferred = async { remoteDataSource.getRoutes() }

        val busStopResult = busStopDeferred.await()
        val routeResult = routeDeferred.await()

        when {
            busStopResult is Result.Success && routeResult is Result.Success -> {
                val busStops = busStopResult.data
                val routes = routeResult.data

                localDataSource.syncInitialData(
                    busStops = busStops.map { it.toBusStopEntity() },
                    routes = routes.map { it.toRouteEntity() }
                )

                InitialDataResult(
                    busStops = busStops.map { it.toBusStopDomain() },
                    routes = routes.map { it.toRouteDomain() },
                    isOffline = false,
                    error = null
                )
            }
            else -> {
                InitialDataResult(
                    busStops = localDataSource.getBusStops().map { it.toBusStopDomain() },
                    routes = localDataSource.getRoutes().map { it.toRouteDomain() },
                    isOffline = true,
                    error = when {
                        busStopResult is Result.Error -> busStopResult.error
                        routeResult is Result.Error -> routeResult.error
                        else -> NetworkErrorType.UNKNOWN
                    }
                )
            }
        }
    }

//    override suspend fun getInitialData(): InitialDataResult {
//        return when (val result = remoteDataSource.getBusStops()) {
//            is Result.Success -> {
//                val busStops = result.data + listOf(BusStopResponseDto(
//                    id = 99,
//                    name = "Halte Kalideres",
//                    latitude = -6.1537810,
//                    longitude = 106.7084305
//                ))
//
//                localDataSource.updateBusStops(busStops.map { it.toBusStopEntity() })
//                InitialDataResult(
//                    data = busStops.map { it.toBusStopDomain() },
//                    isOffline = false,
//                    error = null
//                )
//            }
//            is Result.Error -> {
//                InitialDataResult(
//                    data = localDataSource.getBusStops().map { it.toBusStopDomain() },
//                    isOffline = true,
//                    error = result.error
//                )
//            }
//        }
//    }

    override suspend fun getGeofenceStatus(): Flow<GeofenceState> =
        localDataSource.getGeofenceStatus()

    override suspend fun getCurrentBusStopId(): Flow<Int> =
        localDataSource.getCurrentBusStopId()

    override suspend fun getCurrentBusStopLocation(): Flow<Coordinate> =
        localDataSource.getCurrentBusStopLocation()

    override suspend fun setGeofenceStatus(geofence: GeofenceState) =
        localDataSource.setGeofenceStatus(geofence)

    override suspend fun setCurrentBusStopId(id: Int) =
        localDataSource.setCurrentBusStopId(id)

    override suspend fun setCurrentBusStopLocation(coordinate: Coordinate) =
        localDataSource.setCurrentBusStopLocation(coordinate)
}