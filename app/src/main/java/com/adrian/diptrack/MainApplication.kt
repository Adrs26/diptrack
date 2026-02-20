package com.adrian.diptrack

import android.app.Application
import com.adrian.diptrack.data.di.dataModule
import com.adrian.diptrack.data.di.databaseModule
import com.adrian.diptrack.data.di.domainModule
import com.adrian.diptrack.data.di.networkModule
import com.adrian.diptrack.presentation.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(
                listOf(
                    databaseModule,
                    dataModule,
                    domainModule,
                    networkModule,
                    viewModelModule
                )
            )
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.coroutineContext.cancelChildren()
    }
}