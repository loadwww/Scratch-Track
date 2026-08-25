package com.caiji.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.caiji.app.data.prefs.BackgroundTheme
import com.caiji.app.ui.components.FunctionEntry
import com.caiji.app.ui.components.MarqueeText
import com.caiji.app.ui.components.StatCard
import com.caiji.app.ui.theme.InvestRed
import com.caiji.app.ui.theme.LossOrange
import com.caiji.app.ui.theme.ProfitGold
import com.caiji.app.ui.theme.WinGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenDiary: () -> Unit,
    onOpenHighlight: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddLottery: () -> Unit,
    onOpenRandom: () -> Unit,
    onOpenScratch: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val bg = backgroundBrush(state.settings.background, state.isBudgetNearLimit, state.isBudgetExceeded)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Scratch Track",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLottery, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Outlined.AddCircle, contentDescription = "添加记录", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 背景层：壁纸优先，否则纯色渐变
            val wallpaperPath = state.settings.wallpaperPath
            if (wallpaperPath.isNotBlank() && File(wallpaperPath).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(File(wallpaperPath)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                )
            } else {
                Box(Modifier.fillMaxSize().background(bg))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 预算提醒
                if (state.isBudgetExceeded) {
                    BudgetAlertCard(
                        message = state.settings.budgetExceedAlert,
                        color = InvestRed
                    )
                } else if (state.isBudgetNearLimit) {
                    BudgetAlertCard(
                        message = state.settings.budgetNearAlert,
                        color = LossOrange
                    )
                }

                // 顶部统计 - 当月数据 + 盈亏百分比
                MonthStatCardRow(
                    monthLabel = state.monthLabel,
                    monthInvest = state.monthInvest,
                    monthWin = state.monthWin,
                    monthPnl = state.monthPnl
                )

                // 月度预算进度条（整行）
                BudgetProgressBar(
                    invest = state.monthInvest,
                    budget = state.monthBudget,
                    monthLabel = state.monthLabel
                )

                // 轮换提醒文字
                MarqueeText(
                    text = state.marqueeText,
                    onClick = { vm.rotateMarquee() }
                )

                // 功能入口
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FunctionEntry(
                        icon = Icons.Outlined.Book,
                        title = "彩票日记",
                        subtitle = "记录每次投注心情",
                        onClick = onOpenDiary,
                        modifier = Modifier.weight(1f)
                    )
                    FunctionEntry(
                        icon = Icons.Outlined.PhotoLibrary,
                        title = "高光时刻",
                        subtitle = "珍藏大奖瞬间",
                        onClick = onOpenHighlight,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FunctionEntry(
                        icon = Icons.Outlined.Casino,
                        title = "随机数",
                        subtitle = "生成幸运数字",
                        onClick = onOpenRandom,
                        modifier = Modifier.weight(1f)
                    )
                    FunctionEntry(
                        icon = Icons.Outlined.ShowChart,
                        title = "历史盈亏",
                        subtitle = "趋势图表分析",
                        onClick = onOpenChart,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FunctionEntry(
                        icon = Icons.Outlined.Science,
                        title = "刮刮乐",
                        subtitle = "虚拟刮奖体验",
                        onClick = onOpenScratch,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetAlertCard(message: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BudgetProgressBar(
    invest: Double,
    budget: Double,
    monthLabel: String
) {
    val pct = if (budget <= 0) 0.0 else invest / budget * 100
    val progress = (pct / 100f).toFloat().coerceIn(0f, 1f)
    // 颜色分段：绿色<60% / 蓝色60-80% / 黄色80-100% / 深红色>100%
    val barColor = when {
        budget <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        pct >= 100 -> Color(0xFFB71C1C) // 深红色
        pct >= 80 -> Color(0xFFF9A825)  // 黄色
        pct >= 60 -> Color(0xFF1976D2)  // 蓝色
        else -> WinGreen               // 绿色
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$monthLabel 预算",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "¥${"%.2f".format(invest)} / ¥${"%.2f".format(budget)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = barColor,
            trackColor = if (pct >= 100) barColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text(
            text = "使用率 %.2f%%".format(pct),
            style = MaterialTheme.typography.labelSmall,
            color = barColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun MonthStatCardRow(
    monthLabel: String,
    monthInvest: Double,
    monthWin: Double,
    monthPnl: Double
) {
    val investColor = InvestRed
    val winColor = WinGreen
    val pnlColor = if (monthPnl > 0) WinGreen else if (monthPnl < 0) InvestRed else MaterialTheme.colorScheme.onSurfaceVariant

    val pnlPct = if (monthInvest > 0) monthPnl / monthInvest * 100 else 0.0
    val pnlSub = if (monthInvest > 0) {
        (if (pnlPct >= 0) "+" else "") + "%.2f%%".format(pnlPct)
    } else "0.00%"

    Column {
        // 月份概览 + 盈亏比同一行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$monthLabel 月概览",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "盈亏比 $pnlSub",
                style = MaterialTheme.typography.labelMedium,
                color = pnlColor,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatCard(
                label = "本月投入",
                value = formatMoney(monthInvest),
                accent = investColor,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "本月中奖",
                value = formatMoney(monthWin),
                accent = winColor,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "本月盈亏",
                value = (if (monthPnl >= 0) "+" else "") + "%.2f".format(monthPnl),
                accent = pnlColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatMoney(amount: Double): String {
    return if (amount == 0.0) "¥0" else "¥%.2f".format(amount)
}

/** 根据用户偏好 + 预算状态合成首页背景画刷 */
private fun backgroundBrush(
    theme: BackgroundTheme,
    nearLimit: Boolean,
    exceeded: Boolean
): Brush {
    val (a, b) = when (theme) {
        BackgroundTheme.WARM -> Color(0xFFFFF1F0) to Color(0xFFFFE3E0)
        BackgroundTheme.COOL -> Color(0xFFEAF3FF) to Color(0xFFDDEBFF)
        BackgroundTheme.DARK -> Color(0xFF121117) to Color(0xFF1B1B22)
        BackgroundTheme.CRIMSON -> Color(0xFFFFEBEE) to Color(0xFFFFCDD2)
        BackgroundTheme.DEFAULT -> Color(0xFFF7F2FB) to Color(0xFFEBE3F6)
    }
    val tinted = if (exceeded) a.copy(red = 1f, green = a.green * 0.97f, blue = a.blue * 0.97f)
    else if (nearLimit) a.copy(red = 1f, green = 0.97f, blue = 0.9f) else a
    return Brush.verticalGradient(listOf(tinted, b))
}
