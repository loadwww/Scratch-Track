package com.caiji.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.entity.MonthlyBudget
import com.caiji.app.data.prefs.SettingsStore
import com.caiji.app.data.repository.BudgetRepository
import com.caiji.app.data.repository.LotteryRepository
import com.caiji.app.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetUiState(
    val month: String = DateUtils.currentMonth(),
    val defaultBudget: Double = 500.0,
    val currentBudget: Double = 0.0,
    val usedThisMonth: Double = 0.0,
    val allBudgets: List<MonthlyBudget> = emptyList()
) {
    val usageRatio: Float
        get() = if (currentBudget <= 0) 0f else (usedThisMonth / currentBudget).toFloat()
    val isExceeded: Boolean
        get() = currentBudget > 0 && usedThisMonth > currentBudget
}

class BudgetViewModel(
    private val budgetRepo: BudgetRepository,
    private val lotteryRepo: LotteryRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> = combine(
        budgetRepo.observeByMonth(DateUtils.currentMonth()),
        budgetRepo.observeAll(),
        lotteryRepo.observeInvestThisMonth(),
        settingsStore.settings
    ) { current, all, used, settings ->
        BudgetUiState(
            month = DateUtils.currentMonth(),
            defaultBudget = settings.monthlyBudgetDefault,
            currentBudget = current?.budgetAmount ?: settings.monthlyBudgetDefault,
            usedThisMonth = used,
            allBudgets = all
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BudgetUiState())

    fun saveBudget(month: String, amount: Double) {
        viewModelScope.launch {
            budgetRepo.upsert(MonthlyBudget(month = month, budgetAmount = amount))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                BudgetViewModel(
                    budgetRepo = ServiceLocator.provideBudgetRepo(ctx),
                    lotteryRepo = ServiceLocator.provideLotteryRepo(ctx),
                    settingsStore = ServiceLocator.provideSettings(ctx)
                )
            }
        }
    }
}
