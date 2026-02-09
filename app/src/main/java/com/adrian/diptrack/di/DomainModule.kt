package com.adrian.diptrack.di

import com.adrian.diptrack.domain.usecase.GetBusStopLocationsUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentGeofenceStatusUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetBusStopLocationsUseCase)
    factoryOf(::GetCurrentGeofenceStatusUseCase)
}