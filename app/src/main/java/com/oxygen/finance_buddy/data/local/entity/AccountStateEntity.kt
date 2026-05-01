package com.oxygen.finance_buddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oxygen.finance_buddy.data.local.model.AccountStatePayload

@Entity(tableName = "account_states")
data class AccountStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stateDate: Long,
    val statePayload: AccountStatePayload,
    val createdAt: Long,
    val updatedAt: Long
)

