package com.adrian.diptrack.data.di

import com.adrian.diptrack.domain.usecase.GetBusStopInformationUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusStopUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetBusStopInformationUseCase)
    factoryOf(::GetCurrentBusStopUseCase)
    factoryOf(::GetCurrentUserLocationUseCase)
    factoryOf(::GetFilteredBusStopsUseCase)
    factoryOf(::GetStaticDataUseCase)
    factoryOf(::StartTrackingUseCase)
}
