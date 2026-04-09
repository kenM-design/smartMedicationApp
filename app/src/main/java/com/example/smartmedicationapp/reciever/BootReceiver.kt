package com.example.smartmedicationapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.data.MedicationRepository
import com.example.smartmedicationapp.worker.WorkManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Listens for BOOT_COMPLETED and re-enqueues WorkManager tasks for every
 * medication that has a future reminder time and hasn't been taken yet.
 * WorkManager persists its own DB across reboots, but this receiver handles
 * any edge-cases from older API levels or custom ROMs that may wipe the queue.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val dao  = MedicationDatabase.getDatabase(context).medicationDao()
        val repo = MedicationRepository(dao)

        CoroutineScope(Dispatchers.IO).launch {
            val medications = repo.allMedications.first()
            val now = System.currentTimeMillis()

            medications
                .filter { !it.isTaken && it.reminderTimeMillis > now }
                .forEach { WorkManagerHelper.scheduleMedicationReminder(context, it) }
        }
    }
}