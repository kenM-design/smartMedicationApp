package com.example.smartmedicationapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.data.MedicationRepository
import com.example.smartmedicationapp.worker.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
                .forEach { AlarmScheduler.scheduleMedicationReminder(context, it)  }
        }
    }
}