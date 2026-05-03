package com.oxygen.finance_buddy.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oxygen.finance_buddy.core.security.SecurityPreferences
import com.oxygen.finance_buddy.data.local.AppDatabase
import com.oxygen.finance_buddy.data.local.dao.AccountStateDao
import com.oxygen.finance_buddy.data.local.dao.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add iconKey column to expense_cards
            db.execSQL("ALTER TABLE expense_cards ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'wallet'")
            
            // Recreate expense_items table with new schema
            db.execSQL("""
                CREATE TABLE expense_items_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    cardId INTEGER NOT NULL,
                    expenseDate INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    note TEXT,
                    createdAt INTEGER NOT NULL,
                    isRecurringTemplate INTEGER NOT NULL,
                    recurringParentId INTEGER,
                    recurrenceMonths INTEGER NOT NULL,
                    nextRenewalDate INTEGER,
                    FOREIGN KEY (cardId) REFERENCES expense_cards(id) ON DELETE CASCADE
                )
            """)
            
            // Copy old data to new table
            db.execSQL("""
                INSERT INTO expense_items_new (id, cardId, expenseDate, amount, note, createdAt, isRecurringTemplate, recurringParentId, recurrenceMonths, nextRenewalDate)
                SELECT id, cardId, expenseDate, amount, note, createdAt, 0, NULL, 1, NULL FROM expense_items
            """)
            
            // Drop old table
            db.execSQL("DROP TABLE expense_items")
            
            // Rename new table
            db.execSQL("ALTER TABLE expense_items_new RENAME TO expense_items")
            
            // Recreate indices
            db.execSQL("CREATE INDEX index_expense_items_cardId ON expense_items(cardId)")
            db.execSQL("CREATE INDEX index_expense_items_recurringParentId ON expense_items(recurringParentId)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_buddy.db"
        ).addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideAccountStateDao(database: AppDatabase): AccountStateDao = database.accountStateDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    @Singleton
    fun provideSecurityPreferences(@ApplicationContext context: Context): SecurityPreferences {
        return SecurityPreferences(context)
    }
}

