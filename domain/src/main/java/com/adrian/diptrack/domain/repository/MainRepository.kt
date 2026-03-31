package com.adrian.diptrack.domain.repository

import com.adrian.diptrack.domain.model.BusSocket
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.domain.model.StaticData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface MainRepository {

    suspend fun getStaticData(): StaticData

    fun getRealtimeBusData(scope: CoroutineScope): SharedFlow<BusSocket>

    fun getCurrentBusStopId(): Flow<Int>

    fun getCurrentBusStopLocation(): Flow<Coordinate>

    suspend fun setCurrentBusStopId(id: Int)

    suspend fun setCurrentBusStopLocation(coordinate: Coordinate)
}
