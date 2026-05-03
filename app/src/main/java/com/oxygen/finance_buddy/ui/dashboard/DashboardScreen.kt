package com.oxygen.finance_buddy.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val states by viewModel.accountStates.collectAsState()
    val expenses by viewModel.allExpenseItems.collectAsState()
    val cards by viewModel.expenseCards.collectAsState()
    val recurringTemplates by viewModel.recurringExpenseTemplates.collectAsState()
    
    val currentBalance = if (states.isNotEmpty()) {
        try {
            val lastState = states.first()
            val cash = lastState.statePayload?.cashRows?.sumOf { it.value * it.count } ?: 0.0
            val bank = lastState.statePayload?.accounts?.sumOf { it.balance } ?: 0.0
            cash + bank
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    } else {
        0.0
    }

    val currentMonthExpenses = remember(expenses) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        expenses.filter { item ->
            cal.timeInMillis = item.expenseDate
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    val expensesByCard = remember(expenses, cards) {
        expenses.groupBy { it.cardId }
            .mapNotNull { (cardId, items) ->
                val cardName = cards.find { it.id == cardId }?.name ?: "Sconosciuto"
                val totalAmount = items.sumOf { it.amount }
                if (totalAmount > 0) Pair(cardName, totalAmount) else null
            }
            .sortedByDescending { it.second }
    }
    
    val totalPieExpenses = expensesByCard.sumOf { it.second }
    val pieColors = listOf(Color(0xFFE91E63), Color(0xFF2196F3), Color(0xFFFFC107), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFF9C27B0))
    val subscriptionBarColor = MaterialTheme.colorScheme.primary
    val annualSubscriptionProjection = remember(recurringTemplates) {
        recurringTemplates.sumOf { template ->
            val months = template.recurrenceMonths.coerceAtLeast(1)
            template.amount * (12.0 / months)
        }
    }
    val recurringItemsByCost = remember(recurringTemplates) {
        recurringTemplates
            .map { template ->
                val months = template.recurrenceMonths.coerceAtLeast(1)
                template.note.orEmpty().ifBlank { "Abbonamento" } to (template.amount * (12.0 / months))
            }
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text("Saldo Totale", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "€ %,.2f".format(currentBalance),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Spese questo mese: € %,.2f".format(currentMonthExpenses), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Statistiche",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Andamento Patrimonio Grafico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (states.size > 1) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            try {
                                val items = states.take(10).reversed()
                                val maxBalance = items.maxOf { s -> 
                                    val cash = s.statePayload?.cashRows?.sumOf { it.value * it.count } ?: 0.0
                                    val bank = s.statePayload?.accounts?.sumOf { it.balance } ?: 0.0
                                    cash + bank
                                }
                                val minBalance = items.minOf { s -> 
                                    val cash = s.statePayload?.cashRows?.sumOf { it.value * it.count } ?: 0.0
                                    val bank = s.statePayload?.accounts?.sumOf { it.balance } ?: 0.0
                                    cash + bank
                                }
                                
                                val pathColor = Color(0xFF4CAF50)
                                val pointSpacing = size.width / (items.size - 1).coerceAtLeast(1)
                                val heightRange = (maxBalance - minBalance).takeIf { it > 0 } ?: 1.0

                                for (i in 0 until items.size - 1) {
                                    val current = items[i]
                                    val next = items[i + 1]

                                    val currentBal = (current.statePayload?.cashRows?.sumOf { it.value * it.count } ?: 0.0) + (current.statePayload?.accounts?.sumOf { it.balance } ?: 0.0)
                                    val nextBal = (next.statePayload?.cashRows?.sumOf { it.value * it.count } ?: 0.0) + (next.statePayload?.accounts?.sumOf { it.balance } ?: 0.0)

                                    val startX = i * pointSpacing
                                    val startY = size.height - ((currentBal - minBalance) / heightRange * size.height).toFloat()
                                    val endX = (i + 1) * pointSpacing
                                    val endY = size.height - ((nextBal - minBalance) / heightRange * size.height).toFloat()

                                    drawLine(
                                        color = pathColor,
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 8f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Aggiungi più snapshot per visualizzare un grafico.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "Spese",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Ripartizione Spese", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (expensesByCard.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    var startAngle = -90f
                                    expensesByCard.forEachIndexed { index, pair ->
                                        val sweepAngle = ((pair.second / totalPieExpenses) * 360f).toFloat()
                                        drawArc(
                                            color = pieColors[index % pieColors.size],
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 40f, cap = StrokeCap.Butt)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                                Text(
                                    text = "€\n${String.format("%.0f", totalPieExpenses)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                expensesByCard.take(5).forEachIndexed { index, pair ->
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).background(pieColors[index % pieColors.size], RoundedCornerShape(2.dp)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(pair.first, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Nessuna spesa registrata per generare il grafico.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text("Proiezione Abbonamenti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("€ %,.2f annui stimati".format(annualSubscriptionProjection), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (recurringItemsByCost.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            val maxCost = recurringItemsByCost.maxOf { it.second }.takeIf { it > 0 } ?: 1.0
                            val barWidth = size.width / recurringItemsByCost.size.coerceAtLeast(1)
                            recurringItemsByCost.take(6).forEachIndexed { index, item ->
                                val barHeight = ((item.second / maxCost) * size.height).toFloat()
                                val left = index * barWidth + barWidth * 0.15f
                                val right = left + barWidth * 0.7f
                                drawRoundRect(
                                    color = subscriptionBarColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(right - left, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Nessun abbonamento impostato.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
