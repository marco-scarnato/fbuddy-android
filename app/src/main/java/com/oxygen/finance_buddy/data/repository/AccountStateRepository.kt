package com.oxygen.finance_buddy.data.repository

import com.oxygen.finance_buddy.data.local.dao.AccountStateDao
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStateRepository @Inject constructor(
    private val accountStateDao: AccountStateDao
) {

    fun getAllAccountStates(): Flow<List<AccountStateEntity>> {
        return accountStateDao.getAllAccountStates()
    }

    fun getAccountStateByDate(startOfDay: Long, endOfDay: Long): Flow<AccountStateEntity?> {
        return accountStateDao.getAccountStateByDate(startOfDay, endOfDay)
    }

    fun getLatestAccountState(): Flow<AccountStateEntity?> {
        return accountStateDao.getLatestAccountState()
    }

    suspend fun insertAccountState(accountState: AccountStateEntity) {
        accountStateDao.insertAccountState(accountState)
    }

    suspend fun deleteAccountState(accountState: AccountStateEntity) {
        accountStateDao.deleteAccountState(accountState)
    }
}
