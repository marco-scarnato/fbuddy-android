package com.oxygen.finance_buddy.ui.spese

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

    val expenseItems: StateFlow<List<ExpenseItemEntity>> = repository.getExpenseItemsForCard(cardId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpenseItem(amount: Double, note: String, date: Long) {
        if (cardId == -1) return
        viewModelScope.launch {
            val item = ExpenseItemEntity(
                cardId = cardId,
                amount = amount,
                note = note.takeIf { it.isNotBlank() },
                expenseDate = date,
                createdAt = System.currentTimeMillis()
            )
            repository.insertExpenseItem(item)
        }
    }

    fun deleteExpenseItem(item: ExpenseItemEntity) {
        viewModelScope.launch {
            repository.deleteExpenseItem(item)
        }
    }
}
