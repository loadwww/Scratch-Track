package com.caiji.app.ui.scratch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.prefs.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class ScratchUiState(
    val coins: Int = 0,
    val totalWinnings: Int = 0,
    val dailyClaimed: Boolean = false,
    val canClaim: Boolean = false,
    val dailyCoinsAmount: Int = 100
)

/** 可选彩票面值 */
enum class TicketFaceValue(val coins: Int, val label: String) {
    T20(20, "20 金币"),
    T30(30, "30 金币"),
    T50(50, "50 金币")
}

/** 奖项倍率 */
enum class PrizeMultiplier(val mult: Int, val label: String) {
    ONE(1, "1倍"),
    TWO(2, "2倍"),
    FIVE(5, "5倍"),
    TEN(10, "10倍"),
    TWENTY(20, "20倍"),
    HUNDRED(100, "100倍")
}

class ScratchViewModel(
    private val store: SettingsStore
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val uiState: StateFlow<ScratchUiState> = store.scratchData.map { data ->
        val today = dateFmt.format(Date())
        val claimedToday = data.lastClaimDate == today
        val dailyCoins = store.settings.first().scratchDailyCoins
        ScratchUiState(
            coins = data.coins,
            totalWinnings = data.totalWinnings,
            dailyClaimed = claimedToday,
            canClaim = !claimedToday,
            dailyCoinsAmount = dailyCoins
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ScratchUiState())

    fun claimDaily() = viewModelScope.launch {
        val today = dateFmt.format(Date())
        val coins = store.settings.first().scratchDailyCoins
        store.addCoins(coins, today)
    }

    /**
     * 刮奖，消耗指定面值的金币。
     * 中奖率约50%，返奖率约65%。
     * 返回中奖金额（0表示未中奖），-1表示金币不足。
     */
    fun scratch(faceValue: Int): Int {
        val current = uiState.value
        if (current.coins < faceValue) return -1

        // 50% 中奖率，返奖率约65%（中奖者E[倍率]=1.3）
        val isWin = Random.nextBoolean()
        val prize = if (!isWin) {
            0
        } else {
            // 按权重分配倍率：满足 p1*1+p2*2+p5*5+p10*10+p20*20+p100*100 ≈ 1.3
            val weights = listOf(
                PrizeMultiplier.ONE to 8450,
                PrizeMultiplier.TWO to 1350,
                PrizeMultiplier.FIVE to 145,
                PrizeMultiplier.TEN to 32,
                PrizeMultiplier.TWENTY to 18,
                PrizeMultiplier.HUNDRED to 5
            )
            val total = weights.sumOf { it.second }
            var roll = Random.nextInt(total)
            var multiplier = 1
            for ((mult, weight) in weights) {
                roll -= weight
                if (roll < 0) {
                    multiplier = mult.mult
                    break
                }
            }
            faceValue * multiplier
        }

        viewModelScope.launch {
            store.scratchResult(faceValue, prize)
        }
        return prize
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                ScratchViewModel(store = ServiceLocator.provideSettings(ctx))
            }
        }
    }
}
