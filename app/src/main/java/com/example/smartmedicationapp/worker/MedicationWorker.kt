package com.example.smartmedicationapp.worker

import android.content.Context
import androidx.work.*
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.data.MedicationRepository
import com.example.smartmedicationapp.notification.NotificationHelper

class MedicationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDICATION_ID   = "medication_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_DOSAGE          = "dosage"

        // Unique work tag prefix – one work request per medication
        fun uniqueWorkName(medicationId: Int) = "medication_reminder_$medicationId"
    }

    override suspend fun doWork(): Result {
        val medicationId   = inputData.getInt(KEY_MEDICATION_ID, -1)
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: return Result.failure()
        val dosage         = inputData.getString(KEY_DOSAGE) ?: return Result.failure()

        if (medicationId == -1) return Result.failure()

        // Verify the medication still exists and hasn't been taken
        val dao = MedicationDatabase.getDatabase(applicationContext).medicationDao()
        val repo = MedicationRepository(dao)
        val medication = repo.getMedicationById(medicationId) ?: return Result.success()

        if (medication.isTaken) return Result.success()

        NotificationHelper.showMedicationReminder(
            context        = applicationContext,
            medicationId   = medicationId,
            medicationName = medicationName,
            dosage         = dosage
        )

        return Result.success()
    }
}