package com.adrian.diptrack.data.di

import com.adrian.diptrack.domain.usecase.CancelTrackingUseCase
import com.adrian.diptrack.domain.usecase.CheckTrackingStatusUseCase
import com.adrian.diptrack.domain.usecase.GetBusDetailUseCase
import com.adrian.diptrack.domain.usecase.GetBusStopDetailUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentBusIdUseCase
import com.adrian.diptrack.domain.usecase.ObserveCurrentModeUseCase
import com.adrian.diptrack.domain.usecase.GetCurrentUserLocationUseCase
import com.adrian.diptrack.domain.usecase.GetDestinationBusStopIdUseCase
import com.adrian.diptrack.domain.usecase.GetDestinationBusStopLocationUseCase
import com.adrian.diptrack.domain.usecase.GetFilteredBusStopsUseCase
import com.adrian.diptrack.domain.usecase.GetOriginBusStopIdUseCase
import com.adrian.diptrack.domain.usecase.ObserveBusLocationUseCase
import com.adrian.diptrack.domain.usecase.GetStaticDataUseCase
import com.adrian.diptrack.domain.usecase.ObserveBusArrivalInfoUseCase
import com.adrian.diptrack.domain.usecase.StartTrackingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::CancelTrackingUseCase)
    factoryOf(::CheckTrackingStatusUseCase)
    factoryOf(::GetBusDetailUseCase)
    factoryOf(::GetBusStopDetailUseCase)
    factoryOf(::GetCurrentBusIdUseCase)
    factoryOf(::GetCurrentUserLocationUseCase)
    factoryOf(::GetDestinationBusStopIdUseCase)
    factoryOf(::GetDestinationBusStopLocationUseCase)
    factoryOf(::GetFilteredBusStopsUseCase)
    factoryOf(::GetOriginBusStopIdUseCase)
    factoryOf(::GetStaticDataUseCase)
    factoryOf(::ObserveBusArrivalInfoUseCase)
    factoryOf(::ObserveBusLocationUseCase)
    factoryOf(::ObserveCurrentModeUseCase)
    factoryOf(::StartTrackingUseCase)
}
