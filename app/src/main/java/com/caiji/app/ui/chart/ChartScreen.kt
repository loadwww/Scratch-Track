package com.caiji.app.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.ui.components.PnLBarChart
import com.caiji.app.ui.theme.InvestRed
import com.caiji.app.ui.theme.LossOrange
import com.caiji.app.ui.theme.ProfitGold
import com.caiji.app.ui.theme.WinGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    vm: ChartViewModel,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史盈亏") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 最近6个月柱形图对比
            ChartCard("最近 6 个月 · 投入/中奖对比") {
                PnLBarChart(
                    bars = state.recentSixMonths,
                    investColor = InvestRed,
                    winColor = WinGreen,
                    unit = 100.0
                )
                LegendRow()
            }

            // 最近6日投入/中奖对比柱形图（只取有记录的最近6天）
            ChartCard("最近 6 日 · 投入/中奖对比") {
                PnLBarChart(
                    bars = state.recentSixDays,
                    investColor = InvestRed,
                    winColor = WinGreen,
                    unit = 50.0
                )
                LegendRow()
            }

            // 6个月以上历史数据条
            ChartCard("更早历史 · 每月数据") {
                if (state.olderMonths.isEmpty()) {
                    Text(
                        "暂无更早的历史数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.olderMonths.forEach { row ->
                            MonthlyDataRow(row = row)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyDataRow(row: MonthlyRow) {
    val pnlColor = if (row.pnl >= 0) ProfitGold else LossOrange
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.month,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = (if (row.pnl >= 0) "+" else "") + "¥${"%.2f".format(row.pnl)}",
                style = MaterialTheme.typography.labelLarge,
                color = pnlColor,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniChip("投入 ¥${"%.2f".format(row.invest)}", InvestRed, Modifier.weight(1f))
            MiniChip("中奖 ¥${"%.2f".format(row.win)}", WinGreen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 4.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun LegendRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                drawRect(InvestRed)
            }
            Text("投入", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                drawRect(WinGreen)
            }
            Text("中奖", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun PnLDayLegendRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                drawRect(WinGreen)
            }
            Text("盈利", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                drawRect(InvestRed)
            }
            Text("亏损", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
