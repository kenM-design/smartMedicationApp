package com.example.smartmedicationapp.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val dao: MedicationDao) {

    val allMedications: Flow<List<Medication>> = dao.getAllMedications()

    suspend fun getMedicationById(id: Int): Medication? = dao.getMedicationById(id)

    suspend fun insertMedication(medication: Medication): Long = dao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) = dao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) = dao.deleteMedication(medication)

    suspend fun markAsTaken(id: Int) = dao.markAsTaken(id)
}