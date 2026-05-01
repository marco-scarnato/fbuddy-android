package com.oxygen.finance_buddy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_cards ORDER BY createdAt DESC")
    fun getAllExpenseCards(): Flow<List<ExpenseCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCard(expenseCard: ExpenseCardEntity)

    @Delete
    suspend fun deleteExpenseCard(expenseCard: ExpenseCardEntity)

    @Query("SELECT * FROM expense_items WHERE cardId = :cardId ORDER BY expenseDate DESC")
    fun getExpenseItemsForCard(cardId: Int): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items ORDER BY expenseDate DESC")
    fun getAllExpenseItems(): Flow<List<ExpenseItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(expenseItem: ExpenseItemEntity)

    @Delete
    suspend fun deleteExpenseItem(expenseItem: ExpenseItemEntity)
}
