package com.oxygen.finance_buddy.data.repository

import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpenseCards(): Flow<List<ExpenseCardEntity>> = expenseDao.getAllExpenseCards()

    suspend fun insertExpenseCard(expenseCard: ExpenseCardEntity) {
        expenseDao.insertExpenseCard(expenseCard)
    }

    suspend fun deleteExpenseCard(expenseCard: ExpenseCardEntity) {
        expenseDao.deleteExpenseCard(expenseCard)
    }

    fun getExpenseItemsForCard(cardId: Int): Flow<List<ExpenseItemEntity>> = expenseDao.getExpenseItemsForCard(cardId)

    fun getAllExpenseItems(): Flow<List<ExpenseItemEntity>> = expenseDao.getAllExpenseItems()

    suspend fun insertExpenseItem(expenseItem: ExpenseItemEntity) {
        expenseDao.insertExpenseItem(expenseItem)
    }

    suspend fun deleteExpenseItem(expenseItem: ExpenseItemEntity) {
        expenseDao.deleteExpenseItem(expenseItem)
    }
}
