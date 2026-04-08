package com.adrian.diptrack.presentation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.domain.repository.MainRepository
import com.adrian.diptrack.domain.usecase.ObserveBusArrivalInfoUseCase
import com.adrian.diptrack.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TrackingService : Service(), KoinComponent {

    private val repository by inject<MainRepository>()
    private val observeBusArrivalInfoUseCase by inject<ObserveBusArrivalInfoUseCase>()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val initialNotification = createInitialNotification()
        startForeground(NOTIFICATION_ID, initialNotification)

        startRealtimeSync()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        repository.setCurrentMode(ModeState.Idle)
        repository.disconnectWebSocket()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply { enableVibration(true) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createInitialNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Pemantauan bus aktif")
            .setContentText("DipTrack sedang memantau lokasi bus di latar belakang")
            .setSmallIcon(R.drawable.ic_directions_bus)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startRealtimeSync() {
        repository.connectWebSocket()
        repository.startRealtimeSync(serviceScope)

        serviceScope.launch {
            repository.currentMode
                .flatMapLatest { mode ->
                    val busId = repository.currentBusId.value
                    val originId = repository.originBusStopId.value
                    val destinationId = repository.destinationBusStopId.value

                    when (mode) {
                        is ModeState.Idle, ModeState.Arrive -> observeBusArrivalInfoUseCase(0, 0)
                        is ModeState.Wait -> observeBusArrivalInfoUseCase(busId, originId)
                        is ModeState.Drive -> observeBusArrivalInfoUseCase(busId, destinationId)
                    }
                }
                .collect { busArrivalInfo ->
                    busArrivalInfo?.let { busArrivalInfo ->
                        updateNotification(busArrivalInfo.busStopName, busArrivalInfo.etaMinutes)
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(busStopName: String, etaMinutes: Int) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Bus menuju $busStopName")
            .setContentText("Estimasi tiba dalam $etaMinutes menit.")
            .setSmallIcon(R.drawable.ic_directions_bus)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Tracking Channel"
    }
}
