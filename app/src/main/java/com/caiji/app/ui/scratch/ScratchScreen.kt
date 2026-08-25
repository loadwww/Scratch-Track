package com.caiji.app.ui.scratch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.ui.theme.InvestRed
import com.caiji.app.ui.theme.ProfitGold
import com.caiji.app.ui.theme.WinGreen
import com.caiji.app.util.ScratchSoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchScreen(
    vm: ScratchViewModel,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 选中的彩票面值
    var selectedFaceValue by remember { mutableStateOf(TicketFaceValue.T20) }

    // 刮刮乐状态
    var scratching by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var currentPrize by remember { mutableStateOf(0) }
    var canScratch by remember { mutableStateOf(false) }
    val erasedPaths = remember { mutableStateListOf<Path>() }
    val currentPath = remember { mutableStateOf(Path()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("刮刮乐体验专区") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 金币信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CoinInfoCard("当前金币", state.coins.toString(), ProfitGold)
                CoinInfoCard("累计中奖", state.totalWinnings.toString(), WinGreen)
            }

            // 每日领取
            if (state.canClaim) {
                Button(
                    onClick = { vm.claimDaily() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("领取每日金币 +${state.dailyCoinsAmount}")
                }
            } else {
                Text(
                    "今日已领取每日金币，明日再来",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 选择彩票面值
            Text(
                "选择彩票面值",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TicketFaceValue.entries.forEach { fv ->
                    FilterChip(
                        selected = selectedFaceValue == fv,
                        onClick = {
                            selectedFaceValue = fv
                            canScratch = false
                            revealed = false
                            erasedPaths.clear()
                            currentPath.value = Path()
                        },
                        label = { Text(fv.label) }
                    )
                }
            }

            // 刮刮乐区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 底层：中奖金额
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (revealed) {
                            if (currentPrize > 0) "恭喜中奖！" else "再接再厉"
                        } else if (canScratch) "???"
                        else "选择面值后开始刮奖",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPrize > 0) ProfitGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (revealed) "$currentPrize 金币" else if (canScratch) "刮开看看" else "",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPrize > 0) ProfitGold else Color.Gray
                    )
                }

                // 顶层：磨砂涂层（可刮开）
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .pointerInput(canScratch, revealed) {
                            if (!canScratch || revealed) return@pointerInput
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: continue
                                    if (change.type != PointerType.Touch && change.type != PointerType.Mouse) continue

                                    if (event.changes.any { it.pressed }) {
                                        if (!scratching) {
                                            scratching = true
                                            ScratchSoundManager.start()
                                        }
                                        val pos = event.changes.first().position
                                        if (currentPath.value.isEmpty) {
                                            currentPath.value.moveTo(pos.x, pos.y)
                                        } else {
                                            currentPath.value.lineTo(pos.x, pos.y)
                                        }
                                        event.changes.forEach { it.consume() }
                                    } else {
                                        if (scratching) {
                                            scratching = false
                                            ScratchSoundManager.stop()
                                            if (!currentPath.value.isEmpty) {
                                                erasedPaths.add(currentPath.value)
                                                currentPath.value = Path()
                                            }
                                            // 检查是否刮得足够多
                                            if (erasedPaths.size >= 3 && !revealed) {
                                                revealed = true
                                                canScratch = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    // 磨砂涂层
                    drawRect(color = Color(0xFFBDBDBD))
                    // 用半透明线条模拟磨砂质感
                    for (i in 0..size.width.toInt() step 8) {
                        drawLine(
                            color = Color(0xFF9E9E9E),
                            start = Offset(i.toFloat(), 0f),
                            end = Offset(i.toFloat() + 4f, size.height),
                            strokeWidth = 1f
                        )
                    }

                    // 擦除路径
                    val allPaths = erasedPaths + currentPath.value
                    allPaths.forEach { path ->
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 60f),
                            blendMode = BlendMode.DstOut
                        )
                    }
                }
            }

            // 操作按钮
            Button(
                onClick = {
                    if (state.coins >= selectedFaceValue.coins) {
                        currentPrize = vm.scratch(selectedFaceValue.coins)
                        if (currentPrize >= 0) {
                            revealed = false
                            canScratch = true
                            erasedPaths.clear()
                            currentPath.value = Path()
                        }
                    }
                },
                enabled = state.coins >= selectedFaceValue.coins && !canScratch,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Casino, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("开始刮奖 (${selectedFaceValue.coins} 金币)")
            }

            if (state.coins < selectedFaceValue.coins) {
                Text(
                    "金币不足！当前 ${state.coins} 金币，需要 ${selectedFaceValue.coins} 金币",
                    color = InvestRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 奖项说明
            Text(
                "奖项：1倍 / 2倍 / 5倍 / 10倍 / 20倍 / 100倍面值　|　中奖率约50%　|　返奖率约65%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                "每日登录领取100枚虚拟金币，中奖金币可累计。全程无真实金钱交易，仅供娱乐。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    // 离开页面时停止音效
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            ScratchSoundManager.stop()
        }
    }
}

@Composable
private fun CoinInfoCard(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
