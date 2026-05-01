package com.oxygen.finance_buddy.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.oxygen.finance_buddy.data.local.dao.AccountStateDao
import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
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
    private val accountStateDao: AccountStateDao,
    private val expenseDao: ExpenseDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val gson = Gson()

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accounts = accountStateDao.getAllAccountStates().first()
                val cards = expenseDao.getAllExpenseCards().first()
                // A bit inefficient but works for backup since we don't have getAllExpenseItems
                val items = mutableListOf<ExpenseItemEntity>()
                for (c in cards) {
                    items.addAll(expenseDao.getExpenseItemsForCard(c.id).first())
                }

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
                        
                        // Restoring data - ideally we should clear first but let's just insert/replace
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
}

