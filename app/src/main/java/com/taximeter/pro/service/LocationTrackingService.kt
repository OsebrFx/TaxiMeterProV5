package com.taximeter.pro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.taximeter.pro.R

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    // Variables pour les données en temps réel
    private var currentFare: Double = 2.5
    private var currentDistance: Double = 0.0
    private var currentTimeSeconds: Int = 0

    companion object {
        const val CHANNEL_ID = "TaxiMeterLocationChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_LOCATION_UPDATE = "com.taximeter.pro.LOCATION_UPDATE"
        const val EXTRA_LOCATION = "extra_location"
        const val ACTION_UPDATE_NOTIFICATION = "com.taximeter.pro.UPDATE_NOTIFICATION"
        const val EXTRA_FARE = "extra_fare"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_TIME = "extra_time"
    }

    private val notificationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            currentFare = intent?.getDoubleExtra(EXTRA_FARE, 2.5) ?: 2.5
            currentDistance = intent?.getDoubleExtra(EXTRA_DISTANCE, 0.0) ?: 0.0
            currentTimeSeconds = intent?.getIntExtra(EXTRA_TIME, 0) ?: 0
            updateNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
        setupLocationCallback()

        // Register receiver for notification updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationUpdateReceiver,
            IntentFilter(ACTION_UPDATE_NOTIFICATION)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("TaxiMeter Pro - Course en cours")
        .setContentText(buildNotificationText())
        .setStyle(NotificationCompat.BigTextStyle().bigText(buildNotificationBigText()))
        .setSmallIcon(R.drawable.ic_taxi)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    private fun updateNotification() {
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationText(): String {
        val minutes = currentTimeSeconds / 60
        return "Tarif: %.2f DH | Distance: %.2f km | %d min".format(currentFare, currentDistance, minutes)
    }

    private fun buildNotificationBigText(): String {
        val minutes = currentTimeSeconds / 60
        val seconds = currentTimeSeconds % 60
        return """
            💰 Tarif: %.2f DH
            📍 Distance: %.2f km
            ⏱️ Durée: %d:%02d

            Course en cours...
        """.trimIndent().format(currentFare, currentDistance, minutes, seconds)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Envoyer la localisation via LocalBroadcast
                    val intent = Intent(ACTION_LOCATION_UPDATE)
                    intent.putExtra(EXTRA_LOCATION, location)
                    LocalBroadcastManager.getInstance(this@LocationTrackingService)
                        .sendBroadcast(intent)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 secondes
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Gérer l'exception de permission
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationUpdateReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}