package com.oxygen.finance_buddy.ui.spese

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Calendar

data class ExpenseCardIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

fun expenseCardIconOptions(): List<ExpenseCardIconOption> = listOf(
    ExpenseCardIconOption("home", "Casa", Icons.Default.Home),
    ExpenseCardIconOption("food", "Cibo", Icons.Default.Restaurant),
    ExpenseCardIconOption("car", "Auto", Icons.Default.DirectionsCar),
    ExpenseCardIconOption("shopping", "Shopping", Icons.Default.ShoppingCart),
    ExpenseCardIconOption("travel", "Viaggi", Icons.Default.Flight),
    ExpenseCardIconOption("movie", "Intrattenimento", Icons.Default.Movie),
    ExpenseCardIconOption("health", "Salute", Icons.Default.Favorite),
    ExpenseCardIconOption("education", "Studio", Icons.Default.School),
    ExpenseCardIconOption("category", "Altro", Icons.Default.Category)
)

fun expenseCardIconOption(key: String): ExpenseCardIconOption {
    return expenseCardIconOptions().firstOrNull { it.key == key } ?: expenseCardIconOptions().last()
}

fun recurringLabel(months: Int): String = when (months.coerceAtLeast(1)) {
    12 -> "Annuale"
    3 -> "Trimestrale"
    else -> "Mensile"
}

fun addMonthsToDate(date: Long, months: Int): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    calendar.add(Calendar.MONTH, months.coerceAtLeast(1))
    return calendar.timeInMillis
}