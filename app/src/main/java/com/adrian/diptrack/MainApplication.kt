package com.adrian.diptrack

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adrian.diptrack.data.di.dataModule
import com.adrian.diptrack.data.di.databaseModule
import com.adrian.diptrack.data.di.domainModule
import com.adrian.diptrack.data.di.networkModule
import com.adrian.diptrack.data.local.datastore.DipTrackPreferences
import com.adrian.diptrack.data.manager.SocketManager
import com.adrian.diptrack.domain.model.Coordinate
import com.adrian.diptrack.presentation.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application(), DefaultLifecycleObserver {

    private val socketManager by inject<SocketManager>()
    private val dipTrackPreferences by inject<DipTrackPreferences>()

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super<Application>.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

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

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        applicationScope.launch {
            dipTrackPreferences.setCurrentBusStopId(0)
            dipTrackPreferences.setCurrentBusStopLocation(Coordinate(0.0, 0.0))
        }
        socketManager.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        socketManager.disconnect()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.coroutineContext.cancelChildren()
    }
}