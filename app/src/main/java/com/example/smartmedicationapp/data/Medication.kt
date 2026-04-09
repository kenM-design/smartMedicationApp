package com.example.smartmedicationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency { DAILY, WEEKLY }

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String,           // e.g. "500mg"
    val frequency: Frequency,
    val reminderTimeMillis: Long, // epoch millis for next reminder
    val isTaken: Boolean = false
)