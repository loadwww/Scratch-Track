package com.caiji.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 首页顶部"投入 / 中奖 / 盈亏"统计卡片。
 * 三栏横向并列。
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    sub: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (sub != null) {
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun StatCardRow(
    totalInvest: Double,
    totalWin: Double,
    investColor: Color,
    winColor: Color,
    pnlColor: Color,
    modifier: Modifier = Modifier
) {
    val diff = totalWin - totalInvest
    val pnlSub = if (totalInvest > 0.0) {
        val pct = diff / totalInvest * 100
        (if (pct >= 0) "+" else "") + "%.2f%%".format(pct)
    } else "0.0%"

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        StatCard(
            label = "总投入",
            value = formatMoney(totalInvest),
            accent = investColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "总中奖",
            value = formatMoney(totalWin),
            accent = winColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "净盈亏",
            value = (if (diff >= 0) "+" else "") + "%.2f".format(diff),
            sub = pnlSub,
            accent = pnlColor,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatMoney(amount: Double): String {
    val abs = if (amount == 0.0) "0" else "%.2f".format(amount)
    return "¥$abs"
}
