package com.monghit.familytrack.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.monghit.familytrack.FamilyTrackApp
import com.monghit.familytrack.MainActivity
import com.monghit.familytrack.R
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.domain.model.Location
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var locationCallback: LocationCallback? = null
    private var currentInterval: Long = 300_000L // 5 minutes default

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Timber.d("LocationForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_INTERVAL -> {
                val newSeconds = intent.getIntExtra(EXTRA_INTERVAL_SECONDS, 300)
                Timber.d("Updating location interval to ${newSeconds}s")
                updateInterval(newSeconds)
                return START_STICKY
            }
        }

        Timber.d("LocationForegroundService started")
        startForegroundNotification()
        startLocationUpdates()

        return START_STICKY
    }

    private fun startForegroundNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FamilyTrackApp.CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(getString(R.string.notification_service_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.e("Location permission not granted")
            stopSelf()
            return
        }

        serviceScope.launch {
            currentInterval = settingsRepository.locationInterval.first() * 1000L
            Timber.d("Location interval: ${currentInterval / 1000}s")

            val locationRequest = LocationRequest.Builder(currentInterval)
                .setMinUpdateIntervalMillis(currentInterval / 2)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        Timber.d("Location received: ${location.latitude}, ${location.longitude}")
                        sendLocationToServer(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    private fun sendLocationToServer(androidLocation: android.location.Location) {
        serviceScope.launch {
            val userId = settingsRepository.userId.first()
            val deviceId = settingsRepository.deviceId.first()

            val location = Location(
                deviceId = deviceId,
                userId = userId,
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude,
                accuracy = androidLocation.accuracy,
                timestamp = System.currentTimeMillis()
            )

            locationRepository.sendLocation(location)
                .onSuccess {
                    Timber.d("Location sent successfully")
                    settingsRepository.setLastLocationUpdate(System.currentTimeMillis())
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to send location")
                }
        }
    }

    fun updateInterval(newIntervalSeconds: Int) {
        currentInterval = newIntervalSeconds * 1000L
        stopLocationUpdates()
        startLocationUpdates()
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        Timber.d("LocationForegroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_UPDATE_INTERVAL = "com.monghit.familytrack.UPDATE_INTERVAL"
        private const val EXTRA_INTERVAL_SECONDS = "interval_seconds"

        fun start(context: android.content.Context) {
            val intent = Intent(context, LocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, LocationForegroundService::class.java)
            context.stopService(intent)
        }

        fun updateInterval(context: android.content.Context, intervalSeconds: Int) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_UPDATE_INTERVAL
                putExtra(EXTRA_INTERVAL_SECONDS, intervalSeconds)
            }
            context.startService(intent)
        }
    }
}
