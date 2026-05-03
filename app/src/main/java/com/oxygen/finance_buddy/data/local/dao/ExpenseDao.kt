package com.oxygen.finance_buddy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_cards ORDER BY createdAt DESC")
    fun getAllExpenseCards(): Flow<List<ExpenseCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCard(expenseCard: ExpenseCardEntity)

    @Update
    suspend fun updateExpenseCard(expenseCard: ExpenseCardEntity)

    @Delete
    suspend fun deleteExpenseCard(expenseCard: ExpenseCardEntity)

    @Query("SELECT * FROM expense_items WHERE cardId = :cardId ORDER BY expenseDate DESC")
    fun getExpenseItemsForCard(cardId: Int): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE isRecurringTemplate = 0 ORDER BY expenseDate DESC")
    fun getAllExpenseItems(): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE isRecurringTemplate = 1 ORDER BY nextRenewalDate ASC")
    fun getRecurringExpenseTemplates(): Flow<List<ExpenseItemEntity>>

    @Query("SELECT COUNT(*) FROM expense_items WHERE recurringParentId = :parentId AND expenseDate = :expenseDate")
    suspend fun countGeneratedRecurringExpense(parentId: Int, expenseDate: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(expenseItem: ExpenseItemEntity)

    @Update
    suspend fun updateExpenseItem(expenseItem: ExpenseItemEntity)

    @Delete
    suspend fun deleteExpenseItem(expenseItem: ExpenseItemEntity)
}
