package com.oxygen.finance_buddy.ui.spese

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeseScreen(
    viewModel: SpeseViewModel = hiltViewModel(),
    onCardClick: (Int) -> Unit
) {
    val cards by viewModel.expenseCards.collectAsState()
    val allExpenses by viewModel.allExpenseItems.collectAsState()
    val iconOptions = remember { expenseCardIconOptions() }

    val currentMonthExpenses = remember(allExpenses) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        allExpenses.filter { item ->
            cal.timeInMillis = item.expenseDate
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    val expensesByCard = remember(allExpenses, cards) {
        allExpenses.groupBy { it.cardId }
            .mapNotNull { (cardId, items) ->
                val cardName = cards.find { it.id == cardId }?.name ?: "Sconosciuto"
                val totalAmount = items.sumOf { it.amount }
                if (totalAmount > 0) Pair(cardName, totalAmount) else null
            }
            .sortedByDescending { it.second }
    }

    val totalPieExpenses = expensesByCard.sumOf { it.second }
    val pieColors = listOf(Color(0xFFE91E63), Color(0xFF2196F3), Color(0xFFFFC107), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFF9C27B0))

    var showDialog by remember { mutableStateOf(false) }
    var cardName by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf(iconOptions.first().key) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Carte Spesa") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Card")
            }
        }
    ) { padding ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuova Carta Spesa") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Nome della categoria") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Icona", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            iconOptions.forEach { option ->
                                FilterChip(
                                    selected = selectedIconKey == option.key,
                                    onClick = { selectedIconKey = option.key },
                                    label = { Text(option.label) },
                                    leadingIcon = { Icon(option.icon, contentDescription = option.label) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (cardName.isNotBlank()) {
                            viewModel.addExpenseCard(cardName, selectedIconKey)
                            cardName = ""
                            selectedIconKey = iconOptions.first().key
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

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = "Spese",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Andamento Globale (Questo Mese: € %,.2f)".format(currentMonthExpenses), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (expensesByCard.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                        var startAngle = -90f
                                        expensesByCard.forEachIndexed { index, pair ->
                                            val sweepAngle = ((pair.second / totalPieExpenses) * 360f).toFloat()
                                            drawArc(
                                                color = pieColors[index % pieColors.size],
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                style = Stroke(width = 30f, cap = StrokeCap.Butt)
                                            )
                                            startAngle += sweepAngle
                                        }
                                    }
                                    Text(
                                        text = "€\n${String.format("%.0f", totalPieExpenses)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Column(modifier = Modifier.weight(1f).padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    expensesByCard.take(4).forEachIndexed { index, pair ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).background(pieColors[index % pieColors.size], RoundedCornerShape(2.dp)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(pair.first, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nessuna spesa registrata.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                Text("Le tue Carte Spesa:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (cards.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nessuna carta spesa.")
                    }
                }
            } else {
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onCardClick(card.id) }) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(expenseCardIconOption(card.iconKey).icon, contentDescription = card.name, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(card.name, style = MaterialTheme.typography.titleMedium)
                            }

                            val cardTotal = allExpenses.filter { it.cardId == card.id }.sumOf { it.amount }
                            if(cardTotal > 0) {
                                Text("€ %,.2f".format(cardTotal), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                            }

                            IconButton(onClick = { viewModel.deleteExpenseCard(card) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
