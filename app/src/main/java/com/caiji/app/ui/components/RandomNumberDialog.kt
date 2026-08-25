package com.caiji.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * 随机数生成器对话框 - 可自定义最小值与最大值
 */
@Composable
fun RandomNumberDialog(
    onDismiss: () -> Unit
) {
    var minValueText by remember { mutableStateOf("1") }
    var maxValueText by remember { mutableStateOf("100") }
    var resultText by remember { mutableStateOf("--") }
    var rangeError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("随机数生成", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
