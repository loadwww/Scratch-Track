package com.caiji.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.prefs.CaiJiSettings
import com.caiji.app.util.DateUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class HomeUiState(
    val monthInvest: Double = 0.0,
    val monthWin: Double = 0.0,
    val monthPnl: Double = 0.0,
    val monthBudget: Double = 0.0,
    val monthLabel: String = DateUtils.currentMonth(),
    val marqueeText: String = "见好就收",
    val settings: CaiJiSettings = CaiJiSettings()
) {
    @Suppress("unused")
    val wallpaperPath: String get() = settings.wallpaperPath
    /** 本月预算使用比例 0..1（无预算时为 0） */
    val budgetUsage: Float
        get() = if (monthBudget <= 0) 0f else (monthInvest / monthBudget).toFloat()

    /** 是否接近预算上限（≥ 80%） */
    val isBudgetNearLimit: Boolean
        get() = monthBudget > 0 && monthInvest / monthBudget >= 0.8

    /** 是否超过预算 */
    val isBudgetExceeded: Boolean
        get() = monthBudget > 0 && monthInvest >= monthBudget
}

class HomeViewModel(
    private val lotteryRepo: com.caiji.app.data.repository.LotteryRepository,
    private val budgetRepo: com.caiji.app.data.repository.BudgetRepository,
    private val settingsStore: com.caiji.app.data.prefs.SettingsStore
) : ViewModel() {

    private val _marqueeIndex = MutableStateFlow(0)

    private val settingsWithIndex = combine(settingsStore.settings, _marqueeIndex) { s, i -> s to i }

    val uiState: StateFlow<HomeUiState> = combine(
        lotteryRepo.observeInvestThisMonth(),
        lotteryRepo.observeWinThisMonth(),
        lotteryRepo.observePnlThisMonth(),
        budgetRepo.observeByMonth(DateUtils.currentMonth()),
        settingsWithIndex
    ) { monthInvest, monthWin, monthPnl, budget, (settings, idx) ->
        val texts = settings.marqueeTexts.ifEmpty { listOf("见好就收") }
        val text = texts[idx % texts.size]
        HomeUiState(
            monthInvest = monthInvest,
            monthWin = monthWin,
            monthPnl = monthPnl,
            monthBudget = budget?.budgetAmount ?: settings.monthlyBudgetDefault,
            marqueeText = text,
            settings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    /** 点击轮换文字时随机切换到不同的句子 */
    fun rotateMarquee() {
        val texts = uiState.value.settings.marqueeTexts
        if (texts.size <= 1) return
        var next = _marqueeIndex.value
        while (texts.size > 1 && next == _marqueeIndex.value) {
            next = Random.nextInt(texts.size)
        }
        _marqueeIndex.value = next
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                HomeViewModel(
                    lotteryRepo = ServiceLocator.provideLotteryRepo(ctx),
                    budgetRepo = ServiceLocator.provideBudgetRepo(ctx),
                    settingsStore = ServiceLocator.provideSettings(ctx)
                )
            }
        }
    }
}
