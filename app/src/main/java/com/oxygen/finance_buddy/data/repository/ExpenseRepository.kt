package com.oxygen.finance_buddy.data.repository

import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpenseCards(): Flow<List<ExpenseCardEntity>> = expenseDao.getAllExpenseCards()

    suspend fun insertExpenseCard(expenseCard: ExpenseCardEntity) {
        expenseDao.insertExpenseCard(expenseCard)
    }

    suspend fun updateExpenseCard(expenseCard: ExpenseCardEntity) {
        expenseDao.updateExpenseCard(expenseCard)
    }

    suspend fun deleteExpenseCard(expenseCard: ExpenseCardEntity) {
        expenseDao.deleteExpenseCard(expenseCard)
    }

    fun getExpenseItemsForCard(cardId: Int): Flow<List<ExpenseItemEntity>> = expenseDao.getExpenseItemsForCard(cardId)

    fun getAllExpenseItems(): Flow<List<ExpenseItemEntity>> = expenseDao.getAllExpenseItems()

    fun getRecurringExpenseTemplates(): Flow<List<ExpenseItemEntity>> = expenseDao.getRecurringExpenseTemplates()

    suspend fun insertExpenseItem(expenseItem: ExpenseItemEntity) {
        expenseDao.insertExpenseItem(expenseItem)
    }

    suspend fun updateExpenseItem(expenseItem: ExpenseItemEntity) {
        expenseDao.updateExpenseItem(expenseItem)
    }

    suspend fun deleteExpenseItem(expenseItem: ExpenseItemEntity) {
        expenseDao.deleteExpenseItem(expenseItem)
    }

    suspend fun generateDueRecurringExpenses(now: Long = System.currentTimeMillis()) {
        try {
            val recurringTemplates = getRecurringExpenseTemplates().first()
            recurringTemplates.forEach { template ->
                val nextDue = template.nextRenewalDate ?: return@forEach
                var computedNextDue = nextDue

                while (computedNextDue <= now) {
                    try {
                        if (expenseDao.countGeneratedRecurringExpense(template.id, computedNextDue) == 0) {
                            expenseDao.insertExpenseItem(
                                ExpenseItemEntity(
                                    cardId = template.cardId,
                                    expenseDate = computedNextDue,
                                    amount = template.amount,
                                    note = template.note,
                                    createdAt = System.currentTimeMillis(),
                                    recurringParentId = template.id
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    computedNextDue = addMonths(computedNextDue, template.recurrenceMonths)
                }

                try {
                    if (computedNextDue != nextDue) {
                        expenseDao.updateExpenseItem(template.copy(nextRenewalDate = computedNextDue))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addMonths(date: Long, months: Int): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.add(Calendar.MONTH, months.coerceAtLeast(1))
        return calendar.timeInMillis
    }
}
