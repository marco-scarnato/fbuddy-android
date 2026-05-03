package com.oxygen.finance_buddy.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import com.oxygen.finance_buddy.data.local.AppDatabase
import com.oxygen.finance_buddy.data.local.dao.AccountStateDao
import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import com.oxygen.finance_buddy.core.security.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

data class BackupModel(
    val accountStates: List<AccountStateEntity>,
    val expenseCards: List<ExpenseCardEntity>,
    val expenseItems: List<ExpenseItemEntity> // just fetching all for simplicity if possible, but ExpenseDao only exposes items by card. We might need a raw query for all.
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val accountStateDao: AccountStateDao,
    private val expenseDao: ExpenseDao,
    private val securityPreferences: SecurityPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val gson = Gson()
    val biometricEnabled = securityPreferences.isBiometricEnabled
    val pinChangeResult = MutableStateFlow<PinChangeResult?>(null)

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setBiometricEnabled(enabled)
        }
    }

    fun changePin(currentPin: String, newPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedPin = securityPreferences.userPin.first()
                if (savedPin.isNullOrBlank() || savedPin != currentPin) {
                    pinChangeResult.value = PinChangeResult.Error("PIN attuale non corretto")
                    return@launch
                }
                if (newPin.length < 4) {
                    pinChangeResult.value = PinChangeResult.Error("Il nuovo PIN deve avere almeno 4 cifre")
                    return@launch
                }

                securityPreferences.savePin(newPin)
                pinChangeResult.value = PinChangeResult.Success
            } catch (e: Exception) {
                pinChangeResult.value = PinChangeResult.Error("Impossibile aggiornare il PIN")
            }
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accounts = accountStateDao.getAllAccountStates().first()
                val cards = expenseDao.getAllExpenseCards().first()
                val items = expenseDao.getAllExpenseItems().first()

                val backup = BackupModel(accounts, cards, items)
                val jsonStr = gson.toJson(backup)

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    OutputStreamWriter(os).use { writer ->
                        writer.write(jsonStr)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    InputStreamReader(input).use { reader ->
                        val backup = gson.fromJson(reader, BackupModel::class.java)

                        appDatabase.clearAllTables()

                        backup.accountStates.forEach { accountStateDao.insertAccountState(it) }
                        backup.expenseCards.forEach { expenseDao.insertExpenseCard(it) }
                        backup.expenseItems.forEach { expenseDao.insertExpenseItem(it) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appDatabase.clearAllTables()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class PinChangeResult {
    data object Success : PinChangeResult()
    data class Error(val message: String) : PinChangeResult()
}

