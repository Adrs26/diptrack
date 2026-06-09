package com.adrian.diptrack.data.remote.retrofit

import com.adrian.diptrack.data.remote.response.BusResponse
import com.adrian.diptrack.data.remote.response.BusStopResponse
import com.adrian.diptrack.data.remote.response.RouteResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("halte")
    suspend fun getBusStops(): Response<List<BusStopResponse>>

    @GET("jalur")
    suspend fun getRoutes(): Response<List<RouteResponse>>

    @GET("bus")
    suspend fun getBuses(): Response<List<BusResponse>>
}