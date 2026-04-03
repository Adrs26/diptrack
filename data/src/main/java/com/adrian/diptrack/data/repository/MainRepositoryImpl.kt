package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.mapper.toBusEntity
import com.adrian.diptrack.data.mapper.toBusEtaDomain
import com.adrian.diptrack.data.mapper.toBusInformationDomain
import com.adrian.diptrack.data.mapper.toBusLocationDomain
import com.adrian.diptrack.data.mapper.toBusStopDomain
import com.adrian.diptrack.data.mapper.toBusStopEntity
import com.adrian.diptrack.data.mapper.toBusStopEtaEntity
import com.adrian.diptrack.data.mapper.toRouteDomain
import com.adrian.diptrack.data.mapper.toRouteEntity
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusInformation
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : MainRepository {

    override suspend fun getStaticData(): StaticData = coroutineScope {
        val busStopDeferred = async { remoteDataSource.getBusStops() }
        val routeDeferred = async { remoteDataSource.getRoutes() }
        val busDeferred = async { remoteDataSource.getBuses() }

        val busStopResult = busStopDeferred.await()
        val routeResult = routeDeferred.await()
        val busResult = busDeferred.await()

        val allSuccess = busStopResult is Result.Success && routeResult is Result.Success &&
                busResult is Result.Success

        when {
            allSuccess -> {
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
                val buses = busResult.data

                localDataSource.syncInitialData(
                    busStops = busStops.map { it.toBusStopEntity() },
                    routes = routes.map { it.toRouteEntity() },
                    buses = buses.map { it.toBusEntity() }
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
                        busResult is Result.Error -> busResult.error
                        else -> NetworkErrorType.UNKNOWN
                    }
                )
            }
        }
    }

    override fun getRealtimeBusLocation(busId: Int?): Flow<List<BusLocation>> = channelFlow {
        val lastLocations = mutableMapOf<Int, Pair<Double, Double>>()

        launch {
            remoteDataSource.getRealtimeBusData().collect { remoteData ->
                val last = lastLocations[remoteData.id]

                val prevLat = last?.first ?: remoteData.latitude
                val prevLng = last?.second ?: remoteData.longitude

                val currLat = remoteData.latitude
                val currLng = remoteData.longitude

                lastLocations[remoteData.id] = currLat to currLng

                localDataSource.updateBusAndEta(
                    busId = remoteData.id,
                    prevLat = prevLat,
                    prevLng = prevLng,
                    currLat = currLat,
                    currLng = currLng,
                    passenger = remoteData.passenger,
                    nextBusStopName = remoteData.nextBusStopName,
                    etas = remoteData.busStopEtas.map { it.toBusStopEtaEntity() }
                )
            }
        }

        val localFlow = if (busId == null) {
            localDataSource.getBusLocations()
                .map { list -> list.map { it.toBusLocationDomain() } }
        } else {
            localDataSource.getBusLocationById(busId)
                .map { listOf(it.toBusLocationDomain()) }
        }

        localFlow.collect { send(it) }
    }

    override suspend fun getBusStopById(id: Int): BusStop =
        localDataSource.getBusStopById(id).toBusStopDomain()

    override suspend fun getBusEtaForBusStop(busStopId: Int): List<BusEta> =
        localDataSource.getBusEtaForBusStop(busStopId).map { it.toBusEtaDomain() }

    override suspend fun getBusById(id: Int): BusInformation =
        localDataSource.getBusById(id).toBusInformationDomain()

    override suspend fun getNearestAvailableBusId(busStopId: Int): Int? =
        localDataSource.getNearestAvailableBusId(busStopId)

    override fun getCurrentBusStopId(): Flow<Int> =
        localDataSource.getCurrentBusStopId()

    override fun getCurrentBusStopLocation(): Flow<Coordinate> =
        localDataSource.getCurrentBusStopLocation()

    override suspend fun setCurrentBusStopId(id: Int) =
        localDataSource.setCurrentBusStopId(id)

    override suspend fun setCurrentBusStopLocation(coordinate: Coordinate) =
        localDataSource.setCurrentBusStopLocation(coordinate)
}
