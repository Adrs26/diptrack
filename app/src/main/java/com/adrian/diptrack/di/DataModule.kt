package com.adrian.diptrack.di

import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.manager.MapsManagerImpl
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MapsRepositoryImpl
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.repository.MapsRepository
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::DipTrackPreferences)
    singleOf(::LocalDataSource)
    singleOf(::RemoteDataSource)
    singleOf(::MapsManagerImpl).bind(MapsManager::class)
    singleOf(::MapsRepositoryImpl).bind(MapsRepository::class)

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
}