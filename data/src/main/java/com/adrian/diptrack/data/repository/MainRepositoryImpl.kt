package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.mapper.toBusSocketDomain
import com.adrian.diptrack.data.mapper.toBusStopDomain
import com.adrian.diptrack.data.mapper.toBusStopEntity
import com.adrian.diptrack.data.mapper.toRouteDomain
import com.adrian.diptrack.data.mapper.toRouteEntity
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.BusSocket
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class MainRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : MainRepository {

    override suspend fun getStaticData(): StaticData = coroutineScope {
        val busStopDeferred = async { remoteDataSource.getBusStops() }
        val routeDeferred = async { remoteDataSource.getRoutes() }

        val busStopResult = busStopDeferred.await()
        val routeResult = routeDeferred.await()

        when {
            busStopResult is Result.Success && routeResult is Result.Success -> {
                val busStops = busStopResult.data + listOf(BusStopResponse(
                    id = 99,
                    name = "Halte Kalideres",
                    latitude = -6.1537810,
                    longitude = 106.7084305
                )) + listOf(BusStopResponse(
                    id = 100,
                    name = "Halte PJS",
                    latitude = -6.152282,
                    longitude = 106.7179425
                ))
                val routes = routeResult.data

                localDataSource.syncInitialData(
                    busStops = busStops.map { it.toBusStopEntity() },
                    routes = routes.map { it.toRouteEntity() }
                )

                StaticData(
                    busStops = busStops.map { it.toBusStopDomain() },
                    filteredBusStops = busStops.map { it.toBusStopDomain() },
                    routes = routes.map { it.toRouteDomain() },
                    error = null
                )
            }

            else -> {
                StaticData(
                    busStops = localDataSource.getBusStops().map { it.toBusStopDomain() },
                    filteredBusStops = localDataSource.getBusStops().map { it.toBusStopDomain() },
                    routes = localDataSource.getRoutes().map { it.toRouteDomain() },
                    error = when {
                        busStopResult is Result.Error -> busStopResult.error
                        routeResult is Result.Error -> routeResult.error
                        else -> NetworkErrorType.UNKNOWN
                    }
                )
            }
        }
    }

    override fun getRealtimeBusData(scope: CoroutineScope): SharedFlow<BusSocket> =
        remoteDataSource.getRealtimeBusData()
            .map { it.toBusSocketDomain() }
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                replay = 0
            )

    override fun getCurrentBusStopId(): Flow<Int> =
        localDataSource.getCurrentBusStopId()

    override fun getCurrentBusStopLocation(): Flow<Coordinate> =
        localDataSource.getCurrentBusStopLocation()

    override suspend fun setCurrentBusStopId(id: Int) =
        localDataSource.setCurrentBusStopId(id)

    override suspend fun setCurrentBusStopLocation(coordinate: Coordinate) =
        localDataSource.setCurrentBusStopLocation(coordinate)
}
