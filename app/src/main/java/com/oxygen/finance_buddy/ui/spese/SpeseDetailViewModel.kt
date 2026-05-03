package com.oxygen.finance_buddy.ui.spese

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import com.oxygen.finance_buddy.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeseDetailViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardId: Int = savedStateHandle.get<Int>("cardId") ?: -1

    init {
        Log.d("SpeseDetailViewModel", "Initialized with cardId: $cardId")
    }

    val expenseItems: StateFlow<List<ExpenseItemEntity>> = repository.getExpenseItemsForCard(cardId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpenseItem(amount: Double, note: String, date: Long) {
        if (cardId == -1) {
            Log.e("SpeseDetailViewModel", "Cannot add expense: cardId is -1")
            return
        }
        viewModelScope.launch {
            try {
                val item = ExpenseItemEntity(
                    cardId = cardId,
                    amount = amount,
                    note = note.takeIf { it.isNotBlank() },
                    expenseDate = date,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertExpenseItem(item)
                Log.d("SpeseDetailViewModel", "Expense added: $item")
            } catch (e: Exception) {
                Log.e("SpeseDetailViewModel", "Error adding expense", e)
            }
        }
    }

    fun addExpenseItem(
        amount: Double,
        note: String,
        date: Long,
        isRecurringTemplate: Boolean,
        recurrenceMonths: Int
    ) {
        if (cardId == -1) {
            Log.e("SpeseDetailViewModel", "Cannot add expense: cardId is -1")
            return
        }
        viewModelScope.launch {
            try {
                val item = ExpenseItemEntity(
                    cardId = cardId,
                    amount = amount,
                    note = note.takeIf { it.isNotBlank() },
                    expenseDate = date,
                    createdAt = System.currentTimeMillis(),
                    isRecurringTemplate = isRecurringTemplate,
                    recurrenceMonths = recurrenceMonths,
                    nextRenewalDate = if (isRecurringTemplate) addMonthsToDate(date, recurrenceMonths) else null
                )
                repository.insertExpenseItem(item)
                Log.d("SpeseDetailViewModel", "Recurring expense added: $item")
            } catch (e: Exception) {
                Log.e("SpeseDetailViewModel", "Error adding recurring expense", e)
            }
        }
    }

    fun deleteExpenseItem(item: ExpenseItemEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExpenseItem(item)
                Log.d("SpeseDetailViewModel", "Expense deleted: ${item.id}")
            } catch (e: Exception) {
                Log.e("SpeseDetailViewModel", "Error deleting expense", e)
            }
        }
    }

    private fun addMonthsToDate(date: Long, months: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = date }
        calendar.add(java.util.Calendar.MONTH, months.coerceAtLeast(1))
        return calendar.timeInMillis
    }
}
