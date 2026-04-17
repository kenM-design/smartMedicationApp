package com.example.smartmedicationapp.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.smartmedicationapp.data.Medication
import com.example.smartmedicationapp.receiver.AlarmReceiver

object AlarmScheduler {

    private fun buildPendingIntent(context: Context, medication: Medication): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID,   medication.id)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(AlarmReceiver.EXTRA_DOSAGE,          medication.dosage)
        }
        return PendingIntent.getBroadcast(
            context,
            medication.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleMedicationReminder(context: Context, medication: Medication) {
        val triggerAtMillis = medication.reminderTimeMillis
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, medication)

        // setAlarmClock is the most reliable alarm type:
        // - Fires even in Doze mode
        // - Does NOT require SCHEDULE_EXACT_ALARM permission
        // - Shows an alarm clock icon in the status bar (good UX for medication reminders)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelMedicationReminder(context: Context, medication: Medication) {
        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, medication)
        alarmManager.cancel(pendingIntent)
    }
}