package com.example.smartmedicationapp.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartmedicationapp.data.Frequency
import com.example.smartmedicationapp.data.Medication
import com.example.smartmedicationapp.ui.MedicationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    viewModel: MedicationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var name       by remember { mutableStateOf("") }
    var dosage     by remember { mutableStateOf("") }
    var frequency  by remember { mutableStateOf(Frequency.DAILY) }
    var reminderTime by remember { mutableLongStateOf(0L) }
    var timeLabel  by remember { mutableStateOf("Select time") }

    var nameError   by remember { mutableStateOf(false) }
    var dosageError by remember { mutableStateOf(false) }
    var timeError   by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // TimePicker launcher
    val showTimePicker = {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    // If the selected time is already past today, schedule for tomorrow
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                reminderTime = selected.timeInMillis
                timeLabel    = timeFormatter.format(selected.time)
                timeError    = false
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medication", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medication Name
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Medication Name") },
                placeholder   = { Text("e.g. Paracetamol") },
                isError       = nameError,
                supportingText = if (nameError) ({ Text("Name is required") }) else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // Dosage
            OutlinedTextField(
                value         = dosage,
                onValueChange = { dosage = it; dosageError = false },
                label         = { Text("Dosage") },
                placeholder   = { Text("e.g. 500mg") },
                isError       = dosageError,
                supportingText = if (dosageError) ({ Text("Dosage is required") }) else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // Frequency selector
            Text("Frequency", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Frequency.entries.forEach { option ->
                    FilterChip(
                        selected = frequency == option,
                        onClick  = { frequency = option },
                        label    = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Time picker
            Text("Reminder Time", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(
                onClick  = showTimePicker,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(timeLabel)
            }
            if (timeError) {
                Text(
                    "Please select a future time",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    nameError   = name.isBlank()
                    dosageError = dosage.isBlank()
                    timeError   = reminderTime == 0L || reminderTime <= System.currentTimeMillis()

                    if (!nameError && !dosageError && !timeError) {
                        viewModel.addMedication(
                            Medication(
                                name              = name.trim(),
                                dosage            = dosage.trim(),
                                frequency         = frequency,
                                reminderTimeMillis = reminderTime
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Medication")
            }
        }
    }
}