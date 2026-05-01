package com.oxygen.finance_buddy.ui.conti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import com.oxygen.finance_buddy.data.local.model.Account
import com.oxygen.finance_buddy.data.local.model.AccountStatePayload
import com.oxygen.finance_buddy.data.local.model.CashRow
import com.oxygen.finance_buddy.data.repository.AccountStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContiViewModel @Inject constructor(
    private val repository: AccountStateRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    val currentAccountState: StateFlow<AccountStateEntity?> = _selectedDate
        .flatMapLatest { date ->
            val calendar = Calendar.getInstance().apply { timeInMillis = date }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val end = calendar.timeInMillis
            repository.getAccountStateByDate(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val latestAccountState: StateFlow<AccountStateEntity?> = repository.getLatestAccountState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    fun saveAccountState(payload: AccountStatePayload, id: Int = 0) {
        viewModelScope.launch {
            val newState = AccountStateEntity(
                id = id,
                stateDate = _selectedDate.value,
                statePayload = payload,
                createdAt = if (id == 0) System.currentTimeMillis() else currentAccountState.value?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertAccountState(newState)
        }
    }

    fun deleteAccountState(state: AccountStateEntity) {
        viewModelScope.launch {
            repository.deleteAccountState(state)
        }
    }

    fun addMockAccountState() {
        viewModelScope.launch {
            // Snapshot fittizio per dimostrazione senza form
            val mockPayload = AccountStatePayload(
                cashRows = listOf(
                    CashRow(label = "Banconote 50", value = 50.0, count = 2)
                ),
                accounts = listOf(
                    Account(id = 1, name = "Intesa Sanpaolo", balance = 1250.50, color = "#123456"),
                    Account(id = 2, name = "Carta Prepagata", balance = 300.00, color = "#FF0000")
                )
            )

            val newState = AccountStateEntity(
                stateDate = System.currentTimeMillis(),
                statePayload = mockPayload,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            repository.insertAccountState(newState)
        }
    }
}
