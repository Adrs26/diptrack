package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.model.BusEta
import com.adrian.diptrack.domain.model.BusLocation
import com.adrian.diptrack.domain.model.BusStop
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun getStaticData(): StaticData

    fun getRealtimeBusData(): Flow<List<BusLocation>>

    suspend fun getBusStopById(id: Int): BusStop

    fun getBusEtaForBusStop(busStopId: Int): Flow<List<BusEta>>

    fun getCurrentBusStopId(): Flow<Int>

    fun getCurrentBusStopLocation(): Flow<Coordinate>

    suspend fun setCurrentBusStopId(id: Int)

    suspend fun setCurrentBusStopLocation(coordinate: Coordinate)
}
