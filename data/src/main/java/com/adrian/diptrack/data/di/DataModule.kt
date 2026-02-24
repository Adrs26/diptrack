package com.adrian.diptrack.data.di

import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.manager.MapsManagerImpl
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MainRepositoryImpl
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.repository.MainRepository
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.mobile
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::DipTrackPreferences)
    singleOf(::LocalDataSource)
    singleOf(::RemoteDataSource)
    singleOf(::MapsManagerImpl).bind(MapsManager::class)
    singleOf(::MainRepositoryImpl).bind(MainRepository::class)

    single { Geolocator.mobile() }
}
