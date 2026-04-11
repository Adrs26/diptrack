package com.adrian.diptrack.data.di

import com.adrian.diptrack.data.manager.MapsManager
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MainRepository
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    singleOf(::MapsManager)
    singleOf(::RemoteDataSource)
    singleOf(::MainRepository)
}
