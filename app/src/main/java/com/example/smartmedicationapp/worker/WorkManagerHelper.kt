package com.example.smartmedicationapp.worker

import android.content.Context
import androidx.work.*
import com.example.smartmedicationapp.data.Frequency
import com.example.smartmedicationapp.data.Medication
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    private fun buildConstraints() = Constraints.Builder()
        .setRequiresBatteryNotLow(true) // Do not fire on low battery
        .build()

    /**
     * Enqueues a unique WorkRequest for the given medication.
     * Daily   → PeriodicWorkRequest (24 h interval)
     * Weekly  → PeriodicWorkRequest (7-day interval)
     * Both start with an initial delay so the first reminder fires at the scheduled time.
     */
    fun scheduleMedicationReminder(context: Context, medication: Medication) {
        val delayMillis = medication.reminderTimeMillis - System.currentTimeMillis()
        if (delayMillis <= 0) return  // Reminder time is in the past; skip

        val inputData = workDataOf(
            MedicationWorker.KEY_MEDICATION_ID   to medication.id,
            MedicationWorker.KEY_MEDICATION_NAME to medication.name,
            MedicationWorker.KEY_DOSAGE          to medication.dosage
        )

        val constraints = buildConstraints()
        val workName    = MedicationWorker.uniqueWorkName(medication.id)

        when (medication.frequency) {
            Frequency.DAILY -> {
                val request = PeriodicWorkRequestBuilder<MedicationWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .addTag(workName)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    workName,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }

            Frequency.WEEKLY -> {
                val request = PeriodicWorkRequestBuilder<MedicationWorker>(7, TimeUnit.DAYS)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .addTag(workName)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    workName,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }
        }
    }

    fun cancelMedicationReminder(context: Context, medicationId: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(MedicationWorker.uniqueWorkName(medicationId))
    }
}