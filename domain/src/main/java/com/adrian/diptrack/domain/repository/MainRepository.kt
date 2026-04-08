package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.model.BusArrivalInfo
import com.adrian.diptrack.domain.model.BusDetail
import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MainRepository {

    val currentMode: StateFlow<ModeState>

    val originBusStopId: StateFlow<Int>

    val originBusStopLocation: StateFlow<Coordinate>

    val destinationBusStopId: StateFlow<Int>

    val destinationBusStopLocation: StateFlow<Coordinate>

    val currentBusId: StateFlow<Int>

    val previousEta: StateFlow<Int>

    fun setCurrentMode(mode: ModeState)

    fun setOriginBusStopId(id: Int)

    fun setOriginBusStopLocation(location: Coordinate)

    fun setDestinationBusStopId(id: Int)

    fun setDestinationBusStopLocation(location: Coordinate)

    fun setCurrentBusId(id: Int)

    fun setPreviousEta(eta: Int)

    suspend fun getStaticData(): StaticData

    fun connectWebSocket()

    fun disconnectWebSocket()

    fun startRealtimeSync(scope: CoroutineScope)

    fun observeBusLocation(busId: Int?): Flow<List<BusLocation>>

    suspend fun getBusStopById(id: Int): BusStop

    suspend fun getBusEtaForBusStop(busStopId: Int): List<BusEta>

    suspend fun getBusById(id: Int): BusDetail

    fun observeNearestAvailableBus(busStopId: Int): Flow<List<Int>>

    fun observeAllAvailableBusId(busStopId: Int): Flow<List<Int>>

    fun observeBusArrivalInfo(busId: Int, busStopId: Int): Flow<BusArrivalInfo?>
}
