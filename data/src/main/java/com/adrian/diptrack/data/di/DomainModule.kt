package com.adrian.diptrack.data.di

import com.adrian.diptrack.domain.usecase.CheckUserWithinBusStopRadiusUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::CheckUserWithinBusStopRadiusUseCase)
    factoryOf(::GetCurrentBusStopUseCase)
    factoryOf(::GetCurrentUserLocationUseCase)
    factoryOf(::GetFilteredBusStopsUseCase)
    factoryOf(::GetStaticDataUseCase)
}
