package com.oxygen.finance_buddy.ui.conti

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxygen.finance_buddy.data.local.model.Account
import com.oxygen.finance_buddy.data.local.model.AccountStatePayload
import com.oxygen.finance_buddy.data.local.model.CashRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContiScreen(
    viewModel: ContiViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentState by viewModel.currentAccountState.collectAsState()
    val latestState by viewModel.latestAccountState.collectAsState()

    val cashRowsState = rememberSaveable(saver = CashRowsStateSaver) {
        mutableStateOf(emptyList<EditableCashRow>())
    }
    var cashRows by cashRowsState

    val accountsState = rememberSaveable(saver = AccountsStateSaver) {
        mutableStateOf(emptyList<EditableAccount>())
    }
    var accounts by accountsState

    var isDatePickerVisible by remember { mutableStateOf(false) }
    var cashExpanded by remember { mutableStateOf(true) }

    val palette = remember {
        listOf("#43A047", "#1E88E5", "#F4511E", "#8E24AA", "#00897B", "#F9A825")
    }

    // Seed from database only when we have a snapshot for the selected date.
    // If the selected date has no snapshot yet, keep the current draft so edits are not lost.
    LaunchedEffect(currentState, latestState) {
        val source = currentState ?: if (cashRows.isEmpty() && accounts.isEmpty()) latestState else null
        if (source != null) {
            cashRows = source.statePayload.cashRows.map { it.toEditable() }
            accounts = source.statePayload.accounts.map { it.toEditable() }
        } else {
            if (cashRows.isEmpty() && accounts.isEmpty()) {
                cashRows = emptyList()
                accounts = emptyList()
            }
        }
    }

    val cashTotal = cashRows.sumOf { it.valueOrZero() * it.countOrZero() }
    val bankTotal = accounts.sumOf { it.balanceOrZero() }
    val globalTotal = cashTotal + bankTotal

    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setSelectedDate(it) }
                    isDatePickerVisible = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Il mio Patrimonio") },
                actions = {
                    TextButton(onClick = { isDatePickerVisible = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Cambia Data", modifier = Modifier.padding(end = 4.dp))
                        Text(dateStr, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Totale", style = MaterialTheme.typography.labelMedium)
                        Text("€ ${formatAmount(globalTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = {
                        viewModel.saveAccountState(
                            payload = AccountStatePayload(
                                cashRows = cashRows.map { it.toDomain() },
                                accounts = accounts.map { it.toDomain() }
                            ),
                            id = currentState?.id ?: 0
                        )
                    }) {
                        Text("Salva snapshot")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contanti
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { cashExpanded = !cashExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Contanti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("€ ${formatAmount(cashTotal)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Icon(if (cashExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }

                        AnimatedVisibility(visible = cashExpanded) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                cashRows.forEachIndexed { index, row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = row.label,
                                            onValueChange = { newVal -> cashRows = cashRows.toMutableList().apply { this[index] = row.copy(label = newVal) } },
                                            modifier = Modifier.weight(2f),
                                            placeholder = { Text("Nome") },
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = row.valueText,
                                            onValueChange = { newVal -> cashRows = cashRows.toMutableList().apply { this[index] = row.copy(valueText = newVal) } },
                                            modifier = Modifier.weight(1.5f),
                                            placeholder = { Text("Valore") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = row.countText,
                                            onValueChange = { newVal -> cashRows = cashRows.toMutableList().apply { this[index] = row.copy(countText = newVal) } },
                                            modifier = Modifier.weight(1.2f),
                                            placeholder = { Text("Q.ta") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        IconButton(onClick = { cashRows = cashRows.toMutableList().apply { removeAt(index) } }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Elimina")
                                        }
                                    }
                                }
                                TextButton(onClick = { cashRows = cashRows + EditableCashRow() }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Aggiungi Riga")
                                }
                            }
                        }
                    }
                }
            }

            // Conti Bancari
            item {
                Text("Conti Bancari", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            itemsIndexed(accounts) { index, acc ->
                val accountColor = parseHexColor(acc.color)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = accountColor.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).background(accountColor, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = acc.name,
                                onValueChange = { newName -> accounts = accounts.toMutableList().apply { this[index] = acc.copy(name = newName) } },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Nome Conto") },
                                singleLine = true
                            )
                            IconButton(onClick = { accounts = accounts.toMutableList().apply { removeAt(index) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = acc.balanceText,
                            onValueChange = { newVal -> accounts = accounts.toMutableList().apply { this[index] = acc.copy(balanceText = newVal) } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Saldo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            palette.forEach { hex ->
                                val color = parseHexColor(hex)
                                val isSelected = acc.color.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 26.dp else 22.dp)
                                        .background(color, CircleShape)
                                        .clickable { accounts = accounts.toMutableList().apply { this[index] = acc.copy(color = hex) } }
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val nextId = (accounts.maxOfOrNull { it.id } ?: 0) + 1
                        accounts = accounts + EditableAccount(id = nextId, color = palette.first())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Aggiungi Conto")
                }
            }
        }
    }
}

private data class EditableCashRow(
    val label: String = "",
    val valueText: String = "",
    val countText: String = ""
) {
    fun valueOrZero(): Double = valueText.toDoubleOrNullSafe()
    fun countOrZero(): Int = countText.toIntOrNullSafe()
}

private data class EditableAccount(
    val id: Int,
    val name: String = "",
    val balanceText: String = "",
    val color: String
) {
    fun balanceOrZero(): Double = balanceText.toDoubleOrNullSafe()
}

private fun CashRow.toEditable(): EditableCashRow = EditableCashRow(
    label = label,
    valueText = if (value == 0.0) "" else value.toString(),
    countText = if (count == 0) "" else count.toString()
)

private fun Account.toEditable(): EditableAccount = EditableAccount(
    id = id,
    name = name,
    balanceText = if (balance == 0.0) "" else balance.toString(),
    color = color
)

private fun EditableCashRow.toDomain(): CashRow = CashRow(
    label = label,
    value = valueOrZero(),
    count = countOrZero()
)

private fun EditableAccount.toDomain(): Account = Account(
    id = id,
    name = name,
    balance = balanceOrZero(),
    color = color
)

private val CashRowsStateSaver: Saver<androidx.compose.runtime.MutableState<List<EditableCashRow>>, ArrayList<ArrayList<Any?>>> =
    Saver(
        save = { state ->
            ArrayList(
                state.value.map { row ->
                    arrayListOf(row.label, row.valueText, row.countText)
                }
            )
        },
        restore = { restored ->
            mutableStateOf(
                restored.map { row ->
                    EditableCashRow(
                        label = row[0] as String,
                        valueText = row[1] as String,
                        countText = row[2] as String
                    )
                }
            )
        }
    )

private val AccountsStateSaver: Saver<androidx.compose.runtime.MutableState<List<EditableAccount>>, ArrayList<ArrayList<Any?>>> =
    Saver(
        save = { state ->
            ArrayList(
                state.value.map { account ->
                    arrayListOf(account.id, account.name, account.balanceText, account.color)
                }
            )
        },
        restore = { restored ->
            mutableStateOf(
                restored.map { account ->
                    EditableAccount(
                        id = account[0] as Int,
                        name = account[1] as String,
                        balanceText = account[2] as String,
                        color = account[3] as String
                    )
                }
            )
        }
    )

private fun String.toDoubleOrNullSafe(): Double =
    replace(',', '.').toDoubleOrNull() ?: 0.0

private fun String.toIntOrNullSafe(): Int =
    toIntOrNull() ?: 0

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (ex: IllegalArgumentException) {
        Color(0xFF424242)
    }
}

private fun formatAmount(amount: Double): String = "%,.2f".format(amount)
