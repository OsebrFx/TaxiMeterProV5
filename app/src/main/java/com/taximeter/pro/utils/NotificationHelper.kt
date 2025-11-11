package com.taximeter.pro.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.taximeter.pro.R

object NotificationHelper {

    private const val CHANNEL_ID = "TaxiMeterChannel"
    private const val TRIP_END_NOTIFICATION_ID = 2
    private const val PAUSE_NOTIFICATION_ID = 3

    fun showPauseNotification(
        context: Context,
        fare: Double,
        distance: Double,
        timeInSeconds: Int
    ) {
        createNotificationChannel(context)

        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taxi)
            .setContentTitle("⏸️ PAUSE - Course en pause")
            .setContentText("Tarif actuel: %.2f DH".format(fare))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    ⏸️ PAUSE

                    💰 Tarif actuel: %.2f DH
                    📍 Distance: %.2f km
                    ⏱️ Durée: %d:%02d

                    Appuyez sur DÉMARRER pour continuer
                """.trimIndent().format(fare, distance, minutes, seconds)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(PAUSE_NOTIFICATION_ID, notification)
    }

    fun showTripEndNotification(
        context: Context,
        fare: Double,
        distance: Double,
        timeInSeconds: Int
    ) {
        createNotificationChannel(context)

        val minutes = timeInSeconds / 60
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taxi)
            .setContentTitle("Course terminée")
            .setContentText("Tarif: %.1f DH".format(fare))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tarif total: %.1f DH\nDistance: %.1f km\nDurée: %d minutes"
                    .format(fare, distance, minutes)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(TRIP_END_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Notifications TaxiMeter",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications pour les courses de taxi"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}