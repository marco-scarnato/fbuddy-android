package com.oxygen.finance_buddy.ui.spese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import com.oxygen.finance_buddy.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenseCards: StateFlow<List<ExpenseCardEntity>> = repository.getAllExpenseCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenseItems: StateFlow<List<ExpenseItemEntity>> = repository.getAllExpenseItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpenseCard(name: String) {
        viewModelScope.launch {
            val card = ExpenseCardEntity(
                name = name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertExpenseCard(card)
        }
    }

    fun deleteExpenseCard(card: ExpenseCardEntity) {
        viewModelScope.launch {
            repository.deleteExpenseCard(card)
        }
    }

    fun addMockExpenseCard() {
        viewModelScope.launch {
            val card = ExpenseCardEntity(
                name = "Mock Card ${System.currentTimeMillis() % 1000}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertExpenseCard(card)
        }
    }
}
