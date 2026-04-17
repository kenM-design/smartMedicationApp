package com.example.smartmedicationapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartmedicationapp.data.Medication
import com.example.smartmedicationapp.data.MedicationRepository
import com.example.smartmedicationapp.worker.AlarmScheduler
import com.example.smartmedicationapp.worker.WorkManagerHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(
    private val repository: MedicationRepository,
    private val appContext: Context
) : ViewModel() {

    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            val generatedId = repository.insertMedication(medication).toInt()
            // Schedule with the real auto-generated ID
            val savedMedication = medication.copy(id = generatedId)
            AlarmScheduler.scheduleMedicationReminder(appContext, savedMedication)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            AlarmScheduler.cancelMedicationReminder(appContext, medication)
            repository.deleteMedication(medication)
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            // Cancel old reminder
            AlarmScheduler.cancelMedicationReminder(appContext, medication)

            // Update in database
            repository.updateMedication(medication)

            // Schedule new reminder (only if not taken)
            if (!medication.isTaken) {
                AlarmScheduler.scheduleMedicationReminder(appContext, medication)
            }
        }
    }

    fun markAsTaken(medication: Medication) {
        viewModelScope.launch {
            repository.markAsTaken(medication.id)
        }
    }
}

class MedicationViewModelFactory(
    private val repository: MedicationRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}