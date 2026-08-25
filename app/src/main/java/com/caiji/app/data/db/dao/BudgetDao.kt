package com.caiji.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caiji.app.data.db.entity.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: MonthlyBudget): Long

    @Query("SELECT * FROM monthly_budgets WHERE month = :month LIMIT 1")
    suspend fun getByMonth(month: String): MonthlyBudget?

    @Query("SELECT * FROM monthly_budgets ORDER BY month DESC")
    fun observeAll(): Flow<List<MonthlyBudget>>

    @Query("SELECT * FROM monthly_budgets WHERE month = :month LIMIT 1")
    fun observeByMonth(month: String): Flow<MonthlyBudget?>
}
