package com.example.smartmedicationapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.smartmedicationapp.MainActivity
import com.example.smartmedicationapp.R
import com.example.smartmedicationapp.receiver.MarkAsTakenReceiver

object NotificationHelper {

    const val CHANNEL_ID = "medication_reminder_channel"
    const val CHANNEL_NAME = "Medication Reminders"
    const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to take your medications on time"
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showMedicationReminder(
        context: Context,
        medicationId: Int,
        medicationName: String,
        dosage: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tap action – opens MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            medicationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Mark as Taken" action – broadcasts to MarkAsTakenReceiver
        val markTakenIntent = Intent(context, MarkAsTakenReceiver::class.java).apply {
            putExtra(MarkAsTakenReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MarkAsTakenReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_BASE + medicationId)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle(medicationName)
            .setContentText("$dosage – Time to take your meds!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                "Mark as Taken",
                markTakenPendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + medicationId, notification)
    }
}