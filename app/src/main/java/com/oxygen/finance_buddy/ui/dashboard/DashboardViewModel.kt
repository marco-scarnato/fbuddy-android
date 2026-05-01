package com.oxygen.finance_buddy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import com.oxygen.finance_buddy.data.repository.AccountStateRepository
import com.oxygen.finance_buddy.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountStateRepository: AccountStateRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val accountStates: StateFlow<List<AccountStateEntity>> = accountStateRepository.getAllAccountStates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCards: StateFlow<List<ExpenseCardEntity>> = expenseRepository.getAllExpenseCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenseItems: StateFlow<List<ExpenseItemEntity>> = expenseRepository.getAllExpenseItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
