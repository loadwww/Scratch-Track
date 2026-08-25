package com.caiji.app.ui.random

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** 幸运池数据（创建后不可编辑号码） */
data class LuckyPool(
    val name: String,
    val numbers: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomScreen(
    onBack: () -> Unit
) {
    // 基础随机数
    var minValueText by remember { mutableStateOf("1") }
    var maxValueText by remember { mutableStateOf("100") }
    var resultText by remember { mutableStateOf("--") }
    var rangeError by remember { mutableStateOf<String?>(null) }

    // 幸运池列表
    var pools by remember { mutableStateOf(listOf<LuckyPool>()) }
    // 创建对话框
    var showCreateDialog by remember { mutableStateOf(false) }
    // 各池子抽签结果
    var poolResults by remember { mutableStateOf(mapOf<Int, String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("随机数") },
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
            // 基础随机数生成
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("基础随机数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minValueText,
                            onValueChange = {
                                minValueText = it.filter { c -> c.isDigit() || (c == '-' && it.length <= 1) }
                                rangeError = null
                            },
                            label = { Text("最小值") },
                            singleLine = true,
                            isError = rangeError != null,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxValueText,
                            onValueChange = {
                                maxValueText = it.filter { c -> c.isDigit() || (c == '-' && it.length <= 1) }
                                rangeError = null
                            },
                            label = { Text("最大值") },
                            singleLine = true,
                            isError = rangeError != null,
                            supportingText = rangeError?.let { { Text(it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 结果显示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = resultText,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val displayMin = minValueText.ifEmpty { "?" }
                    val displayMax = maxValueText.ifEmpty { "?" }
                    Button(
                        onClick = {
                            val minVal = minValueText.toIntOrNull()
                            val maxVal = maxValueText.toIntOrNull()
                            if (minVal == null || maxVal == null) {
                                rangeError = "请输入有效的整数"
                            } else if (maxVal < minVal) {
                                rangeError = "最大值必须 ≥ 最小值"
                            } else {
                                resultText = Random.nextInt(minVal, maxVal + 1).toString()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("生成随机数 ($displayMin - $displayMax)")
                    }
                }
            }

            // 幸运池
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("幸运池", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (pools.size < 3) {
                            IconButton(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = "添加幸运池")
                            }
                        }
                    }

                    Text(
                        "最多创建3个幸运池。创建时输入任意数量的幸运号码（可重复），创建后不可编辑，随机从中抽取。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    pools.forEachIndexed { index, pool ->
                        LuckyPoolCard(
                            pool = pool,
                            result = poolResults[index],
                            onDraw = {
                                if (pool.numbers.isNotEmpty()) {
                                    val drawn = pool.numbers.random()
                                    poolResults = poolResults.toMutableMap().also { it[index] = drawn }
                                }
                            },
                            onDelete = {
                                pools = pools.toMutableList().also { it.removeAt(index) }
                                poolResults = poolResults.toMutableMap().also { it.remove(index) }
                            }
                        )
                    }

                    if (pools.isEmpty()) {
                        Text(
                            "点击右上角 + 创建幸运池",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // 创建幸运池对话框
    if (showCreateDialog) {
        CreateLuckyPoolDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, numbers ->
                pools = pools + LuckyPool(name = name, numbers = numbers)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CreateLuckyPoolDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    var poolName by remember { mutableStateOf("幸运池") }
    var numberInput by remember { mutableStateOf("") }
    val numbers = remember { mutableStateListOf<String>() }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建幸运池", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = poolName,
                    onValueChange = { poolName = it },
                    label = { Text("幸运池名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = numberInput,
                    onValueChange = {
                        numberInput = it.filter { c -> c.isDigit() || c == '-' }
                        error = null
                    },
                    label = { Text("输入号码（可重复）") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val trimmed = numberInput.trim()
                        if (trimmed.isNotEmpty()) {
                            numbers.add(trimmed)
                            numberInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("添加号码")
                }

                if (numbers.isNotEmpty()) {
                    Text(
                        "已添加 ${numbers.size} 个号码",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // 号码列表（可删除）
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        numbers.forEachIndexed { idx, num ->
                            AssistChip(
                                onClick = { numbers.removeAt(idx) },
                                label = { Text(num) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val name = poolName.trim().ifEmpty { "幸运池" }
                    onCreate(name, numbers.toList())
                }
            ) { Text("创建幸运池") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun LuckyPoolCard(
    pool: LuckyPool,
    result: String?,
    onDraw: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pool.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }

        if (pool.numbers.isNotEmpty()) {
            Text(
                text = "号码：${pool.numbers.joinToString("  ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "此幸运池没有号码",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 结果显示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result ?: "--",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = onDraw,
            enabled = pool.numbers.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Casino, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("抽取幸运数字")
        }
    }
}
