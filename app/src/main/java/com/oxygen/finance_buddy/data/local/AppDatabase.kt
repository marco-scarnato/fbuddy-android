package com.oxygen.finance_buddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oxygen.finance_buddy.data.local.converter.DatabaseConverters
import com.oxygen.finance_buddy.data.local.dao.AccountStateDao
import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseCardEntity
import com.oxygen.finance_buddy.data.local.entity.ExpenseItemEntity

@Database(
    entities = [
        AccountStateEntity::class,
        ExpenseCardEntity::class,
        ExpenseItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountStateDao(): AccountStateDao
    abstract fun expenseDao(): ExpenseDao
}

