package com.adrian.diptrack.di

import com.adrian.diptrack.presentation.screen.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
}