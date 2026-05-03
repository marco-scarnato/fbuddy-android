package com.oxygen.finance_buddy.ui.spese

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeseDetailScreen(
    viewModel: SpeseDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val items by viewModel.expenseItems.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceMonths by remember { mutableStateOf(1) }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio Spese") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedDateMillis = System.currentTimeMillis()
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Spesa")
            }
        }
    ) { padding ->
        if (showDialog) {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuova Spesa") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Importo (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Nota (Opzionale)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Abbonamento", fontWeight = FontWeight.SemiBold)
                                Text("Ripeti automaticamente questa spesa", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        }
                        if (isRecurring) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = recurrenceMonths == 1,
                                    onClick = { recurrenceMonths = 1 },
                                    label = { Text("Mensile") }
                                )
                                FilterChip(
                                    selected = recurrenceMonths == 3,
                                    onClick = { recurrenceMonths = 3 },
                                    label = { Text("Trimestrale") }
                                )
                                FilterChip(
                                    selected = recurrenceMonths == 12,
                                    onClick = { recurrenceMonths = 12 },
                                    label = { Text("Annuale") }
                                )
                            }
                        }
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Data: $dateStr")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Support both . and , as decimal separator
                        val amount = amountText.replace(",", ".").toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.addExpenseItem(amount, noteText, selectedDateMillis, isRecurring, recurrenceMonths)
                            amountText = ""
                            noteText = ""
                            isRecurring = false
                            recurrenceMonths = 1
                            showDialog = false
                        }
                    }) {
                        Text("Aggiungi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Annulla")
                    }
                }
            )
        }

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nessuna spesa inserita in questa carta.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.expenseDate))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.note ?: "Nessuna nota", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (item.isRecurringTemplate) {
                                    Text("Abbonamento ${recurringLabel(item.recurrenceMonths)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(dateStr, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("€ ${item.amount}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.deleteExpenseItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
