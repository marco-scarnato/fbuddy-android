package com.oxygen.finance_buddy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oxygen.finance_buddy.data.local.entity.AccountStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountStateDao {
    @Query("SELECT * FROM account_states ORDER BY stateDate DESC")
    fun getAllAccountStates(): Flow<List<AccountStateEntity>>

    @Query("SELECT * FROM account_states WHERE stateDate >= :startOfDay AND stateDate < :endOfDay LIMIT 1")
    fun getAccountStateByDate(startOfDay: Long, endOfDay: Long): Flow<AccountStateEntity?>

    @Query("SELECT * FROM account_states ORDER BY stateDate DESC LIMIT 1")
    fun getLatestAccountState(): Flow<AccountStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountState(accountState: AccountStateEntity)

    @Delete
    suspend fun deleteAccountState(accountState: AccountStateEntity)
}
