package com.caiji.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 盈亏柱状图 - 每组并排两根柱子（投入、中奖）。
 * @param bars 数据条目
 * @param investColor 投入柱色
 * @param winColor 中奖柱色
 * @param axisColor 轴线颜色
 * @param unit Y 轴刻度步长（>0 时按此单位绘制刻度与标签）
 */
@Composable
fun PnLBarChart(
    bars: List<PnLBar>,
    modifier: Modifier = Modifier,
    investColor: Color = MaterialTheme.colorScheme.error,
    winColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unit: Double = 0.0,
    height: androidx.compose.ui.unit.Dp = 220.dp
) {
    if (bars.isEmpty()) {
        Text(
            text = "暂无数据",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(24.dp)
        )
        return
    }

    val maxVal = (bars.maxOfOrNull { maxOf(it.invest, it.win) } ?: 0.0).coerceAtLeast(1.0)
    val labelColor = axisColor
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = labelColor, fontSize = 9.sp)
    // 左侧留出刻度标签空间
    val labelArea = 34f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        val canvasW = size.width
        val canvasH = size.height
        val axisLeft = labelArea
        val axisBottom = canvasH - 22f
        val axisTop = 8f

        // 计算刻度位置（按 unit）
        val scales = if (unit > 0) {
            val top = (ceil(maxVal / unit) * unit).coerceAtLeast(unit)
            val steps = (top / unit).toInt().coerceIn(1, 12)
            (0..steps).map { it * unit }
        } else {
            (0..4).map { maxVal * it / 4 }
        }
        val effectiveMax = scales.last().coerceAtLeast(1.0)

        // 横向参考线 + Y轴标签
        scales.forEach { v ->
            val ratio = (v / effectiveMax).toFloat()
            val y = axisBottom - (axisBottom - axisTop) * ratio
            drawLine(
                color = axisColor.copy(alpha = 0.18f),
                start = Offset(axisLeft, y),
                end = Offset(canvasW - 4f, y),
                strokeWidth = 1f
            )
            val label = formatAxisValue(v)
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(0f, (y - measured.size.height / 2f).coerceIn(axisTop, axisBottom))
            )
        }

        val groupCount = bars.size
        val groupWidth = (canvasW - axisLeft - 8f) / groupCount
        val barWidth = (groupWidth * 0.32f).coerceAtMost(28f)
        val gap = barWidth * 0.25f

        bars.forEachIndexed { index, bar ->
            val groupLeft = axisLeft + groupWidth * index + (groupWidth - barWidth * 2 - gap) / 2
            val investH = ((bar.invest / effectiveMax) * (axisBottom - axisTop)).toFloat()
            val winH = ((bar.win / effectiveMax) * (axisBottom - axisTop)).toFloat()

            // 投入柱
            drawRoundRect(
                color = investColor,
                topLeft = Offset(groupLeft, axisBottom - investH),
                size = Size(barWidth, investH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // 中奖柱
            drawRoundRect(
                color = winColor,
                topLeft = Offset(groupLeft + barWidth + gap, axisBottom - winH),
                size = Size(barWidth, winH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // X轴标签
            val cx = groupLeft + barWidth + gap / 2
            val labelMeasured = textMeasurer.measure(bar.label, labelStyle)
            drawText(
                textLayoutResult = labelMeasured,
                topLeft = Offset(
                    (cx - labelMeasured.size.width / 2f).coerceIn(0f, canvasW - labelMeasured.size.width),
                    axisBottom + 4f
                )
            )
        }
    }
}

data class PnLBar(
    val label: String,
    val invest: Double,
    val win: Double
)

/**
 * 净盈亏柱状图 - 每天一根柱子，盈为正（向上）、亏为负（向下）。
 * @param points 每条数据（标签、净盈亏）
 * @param unit Y 轴刻度步长
 */
@Composable
fun PnLNetBarChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    positiveColor: Color = Color(0xFF43A047),
    negativeColor: Color = Color(0xFFE53935),
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unit: Double = 0.0,
    height: androidx.compose.ui.unit.Dp = 220.dp
) {
    if (points.isEmpty()) {
        Text(
            text = "暂无数据",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(24.dp)
        )
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = axisColor, fontSize = 9.sp)
    val labelArea = 34f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        val canvasW = size.width
        val canvasH = size.height
        val axisLeft = labelArea
        val axisTop = 8f
        val axisBottom = canvasH - 22f
        val midY = (axisTop + axisBottom) / 2

        val maxAbs = points.maxOfOrNull { maxOf(abs(it.pnl), 1.0) } ?: 1.0
        // 按 unit 对齐上/下边界
        val bound = if (unit > 0) {
            (ceil(maxAbs / unit) * unit).coerceAtLeast(unit)
        } else maxAbs
        val upRange = (midY - axisTop)
        val downRange = (axisBottom - midY)

        // 0 轴
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(axisLeft, midY),
            end = Offset(canvasW - 4f, midY),
            strokeWidth = 1.5f
        )

        // 刻度（正方向与负方向按 unit）
        if (unit > 0) {
            val steps = (bound / unit).toInt().coerceIn(1, 12)
            for (i in 0..steps) {
                val v = i * unit
                // 正向刻度
                val yUp = midY - (v / bound).toFloat() * upRange
                drawLine(
                    color = axisColor.copy(alpha = 0.18f),
                    start = Offset(axisLeft, yUp),
                    end = Offset(canvasW - 4f, yUp),
                    strokeWidth = 1f
                )
                drawAxisLabel(textMeasurer, labelStyle, v, 0f, yUp, axisTop, axisBottom)
                if (v > 0) {
                    // 负向刻度
                    val yDown = midY + (v / bound).toFloat() * downRange
                    drawLine(
                        color = axisColor.copy(alpha = 0.18f),
                        start = Offset(axisLeft, yDown),
                        end = Offset(canvasW - 4f, yDown),
                        strokeWidth = 1f
                    )
                    drawAxisLabel(textMeasurer, labelStyle, -v, 0f, yDown, axisTop, axisBottom)
                }
            }
        } else {
            // 简易 4 等分
            for (i in 0..4) {
                val v = bound * i / 4
                val yUp = midY - (v / bound).toFloat() * upRange
                drawLine(
                    color = axisColor.copy(alpha = 0.18f),
                    start = Offset(axisLeft, yUp),
                    end = Offset(canvasW - 4f, yUp),
                    strokeWidth = 1f
                )
            }
        }

        val groupCount = points.size
        val groupWidth = (canvasW - axisLeft - 8f) / groupCount
        val barWidth = (groupWidth * 0.5f).coerceAtMost(36f)

        points.forEachIndexed { index, p ->
            val cx = axisLeft + groupWidth * index + groupWidth / 2
            val ratio = (abs(p.pnl) / bound).toFloat()
            if (p.pnl >= 0) {
                val h = upRange * ratio
                drawRoundRect(
                    color = positiveColor,
                    topLeft = Offset(cx - barWidth / 2, midY - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            } else {
                val h = downRange * ratio
                drawRoundRect(
                    color = negativeColor,
                    topLeft = Offset(cx - barWidth / 2, midY),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            // X轴标签
            val labelMeasured = textMeasurer.measure(p.label, labelStyle)
            drawText(
                textLayoutResult = labelMeasured,
                topLeft = Offset(
                    (cx - labelMeasured.size.width / 2f).coerceIn(0f, canvasW - labelMeasured.size.width),
                    axisBottom + 4f
                )
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAxisLabel(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    value: Double,
    x: Float,
    y: Float,
    axisTop: Float,
    axisBottom: Float
) {
    val label = formatAxisValue(value)
    val measured = textMeasurer.measure(label, style)
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(x, (y - measured.size.height / 2f).coerceIn(axisTop, axisBottom))
    )
}

private fun formatAxisValue(v: Double): String {
    val n = v.toInt()
    return if (n >= 1000) "%.1fk".format(v / 1000.0) else n.toString()
}

/**
 * 月度盈亏折线图 - 净盈亏曲线 + 0 轴参考线。
 */
@Composable
fun PnLTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    positiveColor: Color = Color(0xFF43A047),
    negativeColor: Color = Color(0xFFE53935),
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    height: androidx.compose.ui.unit.Dp = 220.dp
) {
    if (points.isEmpty()) {
        Text(
            text = "暂无数据",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(24.dp)
        )
        return
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        val canvasW = size.width
        val canvasH = size.height
        val axisLeft = 8f
        val axisRight = canvasW - 8f
        val axisTop = 10f
        val axisBottom = canvasH - 22f

        val maxAbs = (points.maxOfOrNull { maxOf(abs(it.pnl), 1.0) } ?: 1.0)
        val midY = (axisTop + axisBottom) / 2
        val upRange = (midY - axisTop).toFloat()
        val downRange = (axisBottom - midY).toFloat()

        // 0 轴
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(axisLeft, midY),
            end = Offset(axisRight, midY),
            strokeWidth = 1.5f
        )

        // 折线
        val step = if (points.size <= 1) 0f else (axisRight - axisLeft) / (points.size - 1)
        val path = Path()
        points.forEachIndexed { i, p ->
            val x = axisLeft + step * i
            val ratio = (p.pnl / maxAbs).toFloat()
            val y = if (p.pnl >= 0) midY - upRange * ratio else midY + downRange * (-ratio)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = if (points.lastOrNull()?.let { it.pnl >= 0 } == true) positiveColor else negativeColor,
            style = Stroke(width = 3f)
        )

        // 数据点
        points.forEachIndexed { i, p ->
            val x = axisLeft + step * i
            val ratio = (p.pnl / maxAbs).toFloat()
            val y = if (p.pnl >= 0) midY - upRange * ratio else midY + downRange * (-ratio)
            drawCircle(
                color = if (p.pnl >= 0) positiveColor else negativeColor,
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}

data class TrendPoint(
    val label: String,
    val pnl: Double
)
