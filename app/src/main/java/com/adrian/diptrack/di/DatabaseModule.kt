package com.adrian.diptrack.di

import androidx.room.Room
import com.adrian.diptrack.data.local.database.DipTrackDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            DipTrackDatabase::class.java,
            "diptrack.db"
        ).fallbackToDestructiveMigration(true).build()
    }
    single { get<DipTrackDatabase>().mainDao() }
}