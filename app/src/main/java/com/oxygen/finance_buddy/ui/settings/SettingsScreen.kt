package com.oxygen.finance_buddy.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState(initial = false)
    val pinChangeResult by viewModel.pinChangeResult.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportData(it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Impostazioni", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Gestione Dati", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { exportLauncher.launch("finance_buddy_backup.json") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Esporta", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Esporta Dati", fontWeight = FontWeight.Bold)
                        Text("Salva un backup offline (JSON)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Importa", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Importa Dati", fontWeight = FontWeight.Bold)
                        Text("Ripristina un backup precedente", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { showResetConfirm = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Reset", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Reset Dati", fontWeight = FontWeight.Bold)
                        Text("Elimina tutti i conti e le spese", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Sicurezza", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { showPinDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifica PIN", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modifica PIN", fontWeight = FontWeight.Bold)
                        Text("Aggiorna il codice di accesso", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = biometricEnabled, onCheckedChange = { viewModel.setBiometricEnabled(it) })
                }
            }
        }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPinDialog = false
                    pinError = null
                    currentPin = ""
                    newPin = ""
                },
                title = { Text("Modifica PIN") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentPin,
                            onValueChange = { currentPin = it },
                            label = { Text("PIN attuale") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = { Text("Nuovo PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        pinError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        pinError = null
                        if (newPin.length < 4) {
                            pinError = "Inserisci almeno 4 cifre"
                        } else {
                            viewModel.changePin(currentPin, newPin)
                        }
                    }) {
                        Text("Salva")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPinDialog = false
                        pinError = null
                        currentPin = ""
                        newPin = ""
                    }) {
                        Text("Annulla")
                    }
                }
            )
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Conferma reset") },
                text = { Text("Questa azione elimina tutti i conti e le spese. Continuare?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetAllData()
                        showResetConfirm = false
                    }) {
                        Text("Elimina", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Annulla")
                    }
                }
            )
        }

        if (pinChangeResult is PinChangeResult.Success) {
            showPinDialog = false
            currentPin = ""
            newPin = ""
            pinError = null
            viewModel.pinChangeResult.value = null
        } else if (pinChangeResult is PinChangeResult.Error) {
            pinError = (pinChangeResult as PinChangeResult.Error).message
            viewModel.pinChangeResult.value = null
        }
    }
}
