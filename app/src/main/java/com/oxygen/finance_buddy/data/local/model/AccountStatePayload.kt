package com.oxygen.finance_buddy.data.local.model

data class CashRow(
    val label: String,
    val value: Double,
    val count: Int
)

data class Account(
    val id: Int,
    val name: String,
    val balance: Double,
    val color: String
)

data class AccountStatePayload(
    val cashRows: List<CashRow>,
    val accounts: List<Account>
)

