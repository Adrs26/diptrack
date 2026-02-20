package com.adrian.diptrack.presentation.di

import com.adrian.diptrack.presentation.observer.NetworkStatusObserver
import com.adrian.diptrack.presentation.screen.MainViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::NetworkStatusObserver)

    viewModelOf(::MainViewModel)
}