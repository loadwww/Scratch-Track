package com.caiji.app.data.repository

import com.caiji.app.data.db.CaiJiDatabase
import com.caiji.app.data.db.entity.MonthlyBudget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val db: CaiJiDatabase) {

    private val dao get() = db.budgetDao()

    fun observeAll(): Flow<List<MonthlyBudget>> = dao.observeAll()

    fun observeByMonth(month: String): Flow<MonthlyBudget?> = dao.observeByMonth(month)

    suspend fun upsert(budget: MonthlyBudget): Long = dao.upsert(budget)
}
