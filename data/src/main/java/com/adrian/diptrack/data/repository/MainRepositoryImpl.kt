package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.mapper.toDomain
import com.adrian.diptrack.data.mapper.toEntity
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.common.NetworkErrorType
import com.adrian.diptrack.domain.common.Result
import com.adrian.diptrack.domain.common.result.MapsPermissionResult
import com.adrian.diptrack.domain.model.BusArrivalInfo
import com.adrian.diptrack.domain.model.BusDetail
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import com.adrian.diptrack.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainRepositoryImpl(
    private val manager: MapsManager,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : MainRepository {

    private val _currentMode = MutableStateFlow<ModeState>(ModeState.Idle)
    override val currentMode = _currentMode.asStateFlow()

    private val _originBusStopId = MutableStateFlow(0)
    override val originBusStopId = _originBusStopId.asStateFlow()

    private val _originBusStopLocation = MutableStateFlow(Coordinate(0.0, 0.0))
    override val originBusStopLocation = _originBusStopLocation.asStateFlow()

    private val _destinationBusStopId = MutableStateFlow(0)
    override val destinationBusStopId = _destinationBusStopId.asStateFlow()

    private val _destinationBusStopLocation = MutableStateFlow(Coordinate(0.0, 0.0))
    override val destinationBusStopLocation = _destinationBusStopLocation.asStateFlow()

    private val _currentBusId = MutableStateFlow(0)
    override val currentBusId = _currentBusId.asStateFlow()

    private val _previousEta = MutableStateFlow(0)
    override val previousEta = _previousEta.asStateFlow()

    override fun setCurrentMode(mode: ModeState) {
        _currentMode.value = mode
    }

    override fun setOriginBusStopId(id: Int) {
        _originBusStopId.value = id
    }

    override fun setOriginBusStopLocation(location: Coordinate) {
        _originBusStopLocation.value = location
    }

    override fun setDestinationBusStopId(id: Int) {
        _destinationBusStopId.value = id
    }

    override fun setDestinationBusStopLocation(location: Coordinate) {
        _destinationBusStopLocation.value = location
    }

    override fun setCurrentBusId(id: Int) {
        _currentBusId.value = id
    }

    override fun setPreviousEta(eta: Int) {
        _previousEta.value = eta
    }

    override suspend fun getCurrentUserLocation(): MapsPermissionResult =
        manager.getCurrentUserLocation()

    override fun calculateHaversine(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Float = manager.calculateHaversine(startCoordinate, endCoordinate)

    override fun calculateDistanceInKm(
        startCoordinate: Coordinate,
        endCoordinate: Coordinate
    ): Double = manager.calculateDistanceInKm(startCoordinate, endCoordinate)

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
                val busStops = busStopResult.data
                val routes = routeResult.data
                val buses = busResult.data

                localDataSource.syncInitialData(
                    busStops = busStops.map { it.toEntity() },
                    routes = routes.map { it.toEntity() },
                    buses = buses.map { it.toEntity() }
                )

                StaticData(
                    busStops = busStops.map { it.toDomain() },
                    filteredBusStops = busStops.map { it.toDomain() },
                    routes = routes.map { it.toDomain() },
                    error = null
                )
            }

            else -> {
                StaticData(
                    busStops = localDataSource.getBusStops().map { it.toDomain() },
                    filteredBusStops = localDataSource.getBusStops().map { it.toDomain() },
                    routes = localDataSource.getRoutes().map { it.toDomain() },
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

    override fun connectWebSocket() = remoteDataSource.connectWebsocket()

    override fun disconnectWebSocket() = remoteDataSource.disconnectWebsocket()

    override fun startRealtimeSync(scope: CoroutineScope) {
        val lastLocations = mutableMapOf<Int, Pair<Double, Double>>()

        scope.launch {
            remoteDataSource.observeBusData().collect { remoteData ->
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
                    etas = remoteData.busStopEtas.map { it.toEntity() }
                )
            }
        }
    }

    override fun observeBusLocation(busId: Int?): Flow<List<BusLocation>> {
        return if (busId == null) {
            localDataSource.getActiveBusLocations()
                .map { list -> list.map { it.toDomain() } }
        } else {
            localDataSource.getActiveBusLocationById(busId)
                .map { listOf(it.toDomain()) }
        }
    }

    override suspend fun getBusStopById(id: Int): BusStop =
        localDataSource.getBusStopById(id).toDomain()

    override suspend fun getBusEtaForBusStop(busStopId: Int): List<BusEta> =
        localDataSource.getBusEtaForBusStop(busStopId).map { it.toDomain() }

    override suspend fun getBusById(id: Int): BusDetail =
        localDataSource.getBusById(id).toDomain()

    override fun observeNearestAvailableBus(busStopId: Int): Flow<List<Int>> =
        localDataSource.getNearestAvailableBus(busStopId)

    override fun observeAllAvailableBusId(busStopId: Int): Flow<List<Int>> =
        localDataSource.getAllAvailableBusId(busStopId)

    override fun observeBusArrivalInfo(busId: Int, busStopId: Int): Flow<BusArrivalInfo?> =
        localDataSource.observeBusArrivalInfo(busId, busStopId).map { it?.toDomain() }
}
