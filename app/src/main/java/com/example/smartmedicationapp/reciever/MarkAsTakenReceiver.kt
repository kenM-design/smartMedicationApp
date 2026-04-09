package com.example.smartmedicationapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.data.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkAsTakenReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICATION_ID   = "extra_medication_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId   = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (medicationId == -1) return

        // Dismiss the notification immediately
        if (notificationId != -1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }

        // Update the database on IO thread
        val dao  = MedicationDatabase.getDatabase(context).medicationDao()
        val repo = MedicationRepository(dao)

        CoroutineScope(Dispatchers.IO).launch {
            repo.markAsTaken(medicationId)
        }
    }
}