package com.example.smartmedicationapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICATION_ID   = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE          = "extra_dosage"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId   = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: return
        val dosage         = intent.getStringExtra(EXTRA_DOSAGE) ?: return

        if (medicationId == -1) return

        // Use goAsync() so the BroadcastReceiver stays alive during the DB check
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao        = MedicationDatabase.getDatabase(context).medicationDao()
                val medication = dao.getMedicationById(medicationId)

                // Only show if medication still exists and hasn't been taken
                if (medication != null && !medication.isTaken) {
                    NotificationHelper.showMedicationReminder(
                        context        = context,
                        medicationId   = medicationId,
                        medicationName = medicationName,
                        dosage         = dosage
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}