package com.oxygen.finance_buddy.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_items",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"]), Index(value = ["recurringParentId"])]
)
data class ExpenseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val expenseDate: Long,
    val amount: Double,
    val note: String?,
    val createdAt: Long,
    val isRecurringTemplate: Boolean = false,
    val recurringParentId: Int? = null,
    val recurrenceMonths: Int = 1,
    val nextRenewalDate: Long? = null
)
