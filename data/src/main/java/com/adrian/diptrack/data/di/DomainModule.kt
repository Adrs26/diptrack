package com.adrian.diptrack.data.di

import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.IsUserInsideBusStopGeofenceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetFilteredBusStopsUseCase)
    factoryOf(::IsUserInsideBusStopGeofenceUseCase)
}
