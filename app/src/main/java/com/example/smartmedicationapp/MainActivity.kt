package com.example.smartmedicationapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.smartmedicationapp.data.Medication
import com.example.smartmedicationapp.data.MedicationDatabase
import com.example.smartmedicationapp.data.MedicationRepository
import com.example.smartmedicationapp.notification.NotificationHelper
import com.example.smartmedicationapp.ui.MedicationViewModel
import com.example.smartmedicationapp.ui.MedicationViewModelFactory
import com.example.smartmedicationapp.ui.screens.AddMedicationScreen
import com.example.smartmedicationapp.ui.screens.HomeScreen
import com.example.smartmedicationapp.ui.screens.MedicationFormScreen
import com.example.smartmedicationapp.ui.theme.SmartMedicationAppTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Create notification channel once at startup
        NotificationHelper.createNotificationChannel(this)

        // Wire up ViewModel
        val db         = MedicationDatabase.getDatabase(applicationContext)
        val repository = MedicationRepository(db.medicationDao())
        val viewModel  = ViewModelProvider(
            this,
            MedicationViewModelFactory(repository, applicationContext)
        )[MedicationViewModel::class.java]

        setContent {
            SmartMedicationAppTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
private fun AppNavigation(viewModel: MedicationViewModel) {
    var showAddScreen by remember { mutableStateOf(false) }
    var medicationToEdit by remember { mutableStateOf<Medication?>(null) }  // NEW

    when {
        medicationToEdit != null -> {
            MedicationFormScreen(
                viewModel = viewModel,
                medicationToEdit = medicationToEdit,
                onNavigateBack = { medicationToEdit = null }
            )
        }
        showAddScreen -> {
            MedicationFormScreen(
                viewModel = viewModel,
                medicationToEdit = null,
                onNavigateBack = { showAddScreen = false }
            )
        }
        else -> {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { showAddScreen = true },
                onEditClick = { medication -> medicationToEdit = medication }  // NEW
            )
        }
    }
}