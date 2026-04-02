package com.adrian.diptrack.data.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.data.local.source.LocalDataSource
import com.adrian.diptrack.data.manager.MapsManagerImpl
import com.adrian.diptrack.data.remote.source.RemoteDataSource
import com.adrian.diptrack.data.repository.MainRepositoryImpl
import com.adrian.diptrack.domain.manager.MapsManager
import com.adrian.diptrack.domain.repository.MainRepository
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("diptrack_prefs")

val dataModule = module {
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single { DipTrackPreferences(androidContext().dataStore) }

    singleOf(::LocalDataSource)
    singleOf(::RemoteDataSource)
    singleOf(::MapsManagerImpl).bind(MapsManager::class)
    singleOf(::MainRepositoryImpl).bind(MainRepository::class)
}
