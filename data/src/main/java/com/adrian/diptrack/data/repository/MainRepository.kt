package com.adrian.diptrack.data.repository

import com.adrian.diptrack.data.common.ModeState
import com.adrian.diptrack.data.common.model.BusStopDetail
import com.adrian.diptrack.data.common.model.Coordinate
import com.adrian.diptrack.data.common.model.StartTrackingSuccessData
import com.adrian.diptrack.data.common.model.StaticData
import com.adrian.diptrack.data.common.result.CheckTrackingStatusResult
import com.adrian.diptrack.data.common.result.GetBusStopDetailResult
import com.adrian.diptrack.data.common.result.MapsPermissionResult
import com.adrian.diptrack.data.common.result.StartTrackingResult
import com.adrian.diptrack.data.local.dao.MainDao
import com.adrian.diptrack.data.local.entity.BusEntity
import com.adrian.diptrack.data.local.entity.BusStopEntity
import com.adrian.diptrack.data.local.projection.BusArrivalInfoProjection
import com.adrian.diptrack.data.local.projection.BusLocationProjection
import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.mapper.toEntity
import com.adrian.diptrack.data.remote.source.NetworkErrorType
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.remote.source.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

class MainRepository(
    private val manager: MapsManager,
    private val localDataSource: MainDao,
    private val remoteDataSource: RemoteDataSource
) {

    private val _currentMode = MutableStateFlow<ModeState>(ModeState.Idle)
    val currentMode = _currentMode.asStateFlow()

    private val _originBusStopId = MutableStateFlow(0)
    val originBusStopId = _originBusStopId.asStateFlow()

    private val _originBusStopLocation = MutableStateFlow(Coordinate(0.0, 0.0))
    val originBusStopLocation = _originBusStopLocation.asStateFlow()

    private val _destinationBusStopId = MutableStateFlow(0)
    val destinationBusStopId = _destinationBusStopId.asStateFlow()

    private val _destinationBusStopLocation = MutableStateFlow(Coordinate(0.0, 0.0))
    val destinationBusStopLocation = _destinationBusStopLocation.asStateFlow()

    private val _currentBusId = MutableStateFlow(0)
    val currentBusId = _currentBusId.asStateFlow()

    private val _previousEta = MutableStateFlow(0)
    val previousEta = _previousEta.asStateFlow()
    
    fun setCurrentMode(mode: ModeState) {
        _currentMode.value = mode
    }

    fun setOriginBusStopId(id: Int) {
        _originBusStopId.value = id
    }

    fun setOriginBusStopLocation(location: Coordinate) {
        _originBusStopLocation.value = location
    }

    fun setDestinationBusStopId(id: Int) {
        _destinationBusStopId.value = id
    }

    fun setDestinationBusStopLocation(location: Coordinate) {
        _destinationBusStopLocation.value = location
    }

    fun setCurrentBusId(id: Int) {
        _currentBusId.value = id
    }

    fun setPreviousEta(eta: Int) {
        _previousEta.value = eta
    }

    suspend fun getCurrentUserLocation() = manager.getCurrentUserLocation()

    suspend fun getStaticData(): StaticData = coroutineScope {
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
                    busStops = busStops.map { it.toEntity() },
                    filteredBusStops = busStops.map { it.toEntity() },
                    routes = routes.map { it.toEntity() },
                    error = null
                )
            }

            else -> {
                StaticData(
                    busStops = localDataSource.getBusStops(),
                    filteredBusStops = localDataSource.getBusStops(),
                    routes = localDataSource.getRoutes(),
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

    fun connectWebSocket() = remoteDataSource.connectWebsocket()

    fun disconnectWebSocket() = remoteDataSource.disconnectWebsocket()

    fun startRealtimeSync(scope: CoroutineScope) {
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

    fun observeBusLocation(busId: Int?): Flow<List<BusLocationProjection>> {
        return if (busId == null) {
            localDataSource.getActiveBusLocations()
        } else {
            localDataSource.getActiveBusLocationById(busId).map { listOf(it) }
        }
    }

    suspend fun getBusStopDetail(busStopId: Int): GetBusStopDetailResult {
        val userLocation = when (val result = getCurrentUserLocation()) {
            MapsPermissionResult.Denied -> return GetBusStopDetailResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        val busStop = localDataSource.getBusStopById(busStopId)
        val busStopLocation = Coordinate(busStop.latitude, busStop.longitude)
        val busEtas = localDataSource.getBusEtaForBusStop(busStopId)

        val distance = manager.calculateDistanceInKm(userLocation, busStopLocation)

        val busStopInformation = BusStopDetail(
            busStopName = busStop.name,
            distanceKilometers = distance,
            busEtas = busEtas
        )

        return GetBusStopDetailResult.Success(busStopInformation)
    }

    suspend fun getFilteredBusStops(ids: List<Int>): StaticData {
        val initialData = getStaticData()
        val filteredStops = initialData.busStops.filter { busStop -> busStop.id in ids }

        return initialData.copy(filteredBusStops = filteredStops)
    }

    @OptIn(FlowPreview::class)
    suspend fun startTracking(destinationLocation: Coordinate): StartTrackingResult {
        if (currentMode.value is ModeState.Drive) {
            return StartTrackingResult.ChangeDestinationRejected
        }

        val userLocation = when (val result = getCurrentUserLocation()) {
            is MapsPermissionResult.Denied -> return StartTrackingResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        val originLocation =
            when (val result = handleBusStopResolution(userLocation, destinationLocation)) {
                null -> return StartTrackingResult.NotWithinBusStopRadius
                else -> result
            }

        val isSameBusStop = originLocation.latitude.isCloseTo(destinationLocation.latitude) &&
                originLocation.longitude.isCloseTo(destinationLocation.longitude)

        if (isSameBusStop) return StartTrackingResult.SameBusStop

        val currentBusStopId = originBusStopId.value
        val destinationBusStopId = destinationBusStopId.value

        val filteredIds = listOf(currentBusStopId, destinationBusStopId)
        val filteredStops = getFilteredBusStops(filteredIds).filteredBusStops
        val distance = manager.calculateDistanceInKm(originLocation, destinationLocation)

        val busIds = withTimeoutOrNull(10.seconds) {
            localDataSource.getNearestAvailableBus(currentBusStopId)
                .debounce(1.seconds)
                .firstOrNull { it.isNotEmpty() }
        }

        return if (busIds == null) {
            StartTrackingResult.NoAvailableBus
        } else {
            setCurrentMode(ModeState.Idle)
            delay(1.seconds)
            setCurrentMode(ModeState.Wait(busIds[0]))
            setCurrentBusId(busIds[0])
            StartTrackingResult.Success(
                StartTrackingSuccessData(
                    busId = busIds[0],
                    busStops = filteredStops,
                    originBusStopId = currentBusStopId,
                    originBusStopName =
                        filteredStops.firstOrNull { it.id == currentBusStopId }?.name.orEmpty(),
                    destinationBusStopId = destinationBusStopId,
                    destinationBusStopName =
                        filteredStops.firstOrNull { it.id == destinationBusStopId }?.name.orEmpty(),
                    distance = distance
                )
            )
        }
    }

    private suspend fun handleBusStopResolution(
        userLocation: Coordinate,
        destinationLocation: Coordinate
    ): Coordinate? {

        val busStops = getStaticData().busStops

        val result = resolveBusStops(userLocation, destinationLocation, busStops) ?: return null

        val (origin, destination) = result
        val originLocation = Coordinate(origin.latitude, origin.longitude)

        setOriginBusStopId(origin.id)
        setOriginBusStopLocation(originLocation)
        setDestinationBusStopId(destination.id)
        setDestinationBusStopLocation(destinationLocation)

        return originLocation
    }

    private fun resolveBusStops(
        userLocation: Coordinate,
        destinationLocation: Coordinate,
        busStops: List<BusStopEntity>
    ): BusStopSelection? {

        val userCoordinate = Coordinate(userLocation.latitude, userLocation.longitude)

        val originBusStop = busStops.firstOrNull { busStop ->
            val distance = manager.calculateHaversine(
                startCoordinate = userCoordinate,
                endCoordinate = Coordinate(busStop.latitude, busStop.longitude)
            )
            distance <= 100f
        }

        val destinationBusStop = busStops.firstOrNull {
            it.latitude.isCloseTo(destinationLocation.latitude) &&
                it.longitude.isCloseTo(destinationLocation.longitude)
        } ?: return null

        return originBusStop?.let { origin -> BusStopSelection(origin, destinationBusStop) }
    }

    private fun Double.isCloseTo(other: Double) = abs(this - other) < 1e-6

    private data class BusStopSelection(val origin: BusStopEntity, val destination: BusStopEntity)

    suspend fun checkTrackingStatus(
        busLocation: Coordinate,
        seatAvailable: Int,
        eta: Int
    ): CheckTrackingStatusResult {
        val userLocation = when (val result = getCurrentUserLocation()) {
            is MapsPermissionResult.Denied -> return CheckTrackingStatusResult.LocationPermissionDenied
            is MapsPermissionResult.Granted -> result.location
        }

        if (seatAvailable == 0) {
            setCurrentMode(ModeState.Idle)
            setPreviousEta(0)
            return CheckTrackingStatusResult.BusFull
        }

        when (currentMode.value) {
            is ModeState.Drive -> {
                if (!userLocation.isNear(busLocation)) {
                    setCurrentMode(ModeState.Idle)
                    setPreviousEta(0)
                    return CheckTrackingStatusResult.SwitchToIdle
                }
            }
            else -> Unit
        }

        val isEtaBounce = eta.isBounce(previousEta.value)
        val isBusArrived = when (currentMode.value) {
            is ModeState.Wait -> busLocation.isNear(originBusStopLocation.value)
            is ModeState.Drive -> busLocation.isNear(destinationBusStopLocation.value)
            else -> false
        }

        if (isEtaBounce || isBusArrived) {
            when (currentMode.value) {
                is ModeState.Wait -> {
                    setCurrentMode(ModeState.Drive(currentBusId.value))
                    setPreviousEta(0)
                    return CheckTrackingStatusResult.SwitchToDrive
                }
                is ModeState.Drive -> {
                    setCurrentMode(ModeState.Idle)
                    setPreviousEta(0)
                    return CheckTrackingStatusResult.SwitchToArrive
                }
                else -> Unit
            }
        }

        setPreviousEta(eta)
        return CheckTrackingStatusResult.Continue
    }

    private fun Int.isBounce(other: Int): Boolean {
        return other != 0 && this > other && this - other > 1
    }

    private fun Coordinate.isNear(other: Coordinate, threshold: Double = 1e-4): Boolean {
        val latDiff = abs(this.latitude - other.latitude)
        val lonDiff = abs(this.longitude - other.longitude)

        return latDiff < threshold && lonDiff < threshold
    }

    suspend fun cancelTracking(): List<BusStopEntity> {
        _currentMode.value = ModeState.Idle
        return getStaticData().busStops
    }

    suspend fun getBusById(id: Int): BusEntity = localDataSource.getBusById(id)

    fun observeBusArrivalInfo(busId: Int, busStopId: Int): Flow<BusArrivalInfoProjection?> =
        localDataSource.observeBusArrivalInfo(busId, busStopId)
}
