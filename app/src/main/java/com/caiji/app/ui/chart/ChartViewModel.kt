package com.caiji.app.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.dao.MonthlySummaryRow
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.data.repository.LotteryRepository
import com.caiji.app.ui.components.PnLBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale

data class MonthlyRow(
    val month: String,
    val invest: Double,
    val win: Double,
    val pnl: Double = win - invest
)

data class ChartUiState(
    /** 最近6个月（用于柱形图） */
    val recentSixMonths: List<PnLBar> = emptyList(),
    /** 最近6日投入/中奖（只取有记录的最近6天） */
    val recentSixDays: List<PnLBar> = emptyList(),
    /** 6个月之前的历史（用于数据条） */
    val olderMonths: List<MonthlyRow> = emptyList()
)

class ChartViewModel(
    repo: LotteryRepository
) : ViewModel() {

    val uiState: StateFlow<ChartUiState> = combine(
        repo.observeAllMonthlySummary(),
        repo.observeAll()
    ) { all: List<MonthlySummaryRow>, records: List<LotteryRecord> ->
        // 最近6个月柱形图（最新6个月，倒序→升序显示）
        val recent = all.take(6).reversed()
        val older = all.drop(6)

        // 最近6日：按天分组，只取有记录的最近6天（升序显示）
        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFmt = SimpleDateFormat("MM-dd", Locale.getDefault())
        val grouped = records
            .groupBy { dayFmt.format(it.createdAt) }
            .map { (day, list) ->
                DayBucket(
                    day = day,
                    invest = list.sumOf { it.amount },
                    win = list.sumOf { it.winAmount }
                )
            }
            .sortedByDescending { it.day }
            .take(6)
            .sortedBy { it.day }
        val recentSixDays = grouped.map {
            val parsed = dayFmt.parse(it.day)
            val label = if (parsed != null) labelFmt.format(parsed) else it.day
            PnLBar(label = label, invest = it.invest, win = it.win)
        }

        ChartUiState(
            recentSixMonths = recent.map {
                // 将 "yyyy-MM" 转为 "yy-MM" 标签（如 26-08）
                val shortLabel = it.month.substring(2)
                PnLBar(shortLabel, it.invest, it.win)
            },
            recentSixDays = recentSixDays,
            olderMonths = older.map { MonthlyRow(it.month, it.invest, it.win) }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChartUiState())

    private data class DayBucket(val day: String, val invest: Double, val win: Double)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                ChartViewModel(repo = ServiceLocator.provideLotteryRepo(ctx))
            }
        }
    }
}
