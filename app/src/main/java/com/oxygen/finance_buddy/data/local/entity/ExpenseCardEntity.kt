package com.oxygen.finance_buddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_cards")
data class ExpenseCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconKey: String = "wallet",
    val createdAt: Long,
    val updatedAt: Long
)

