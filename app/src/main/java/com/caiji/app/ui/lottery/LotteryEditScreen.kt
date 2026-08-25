package com.caiji.app.ui.lottery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.ui.components.DiaryInputs
import com.caiji.app.util.MusicPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryEditScreen(
    vm: LotteryViewModel,
    recordId: Long?,
    onBack: () -> Unit
) {
    LaunchedEffect(recordId) { vm.load(recordId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.saved) {
        if (state.saved) {
            // 盈利时播放庆祝音乐
            val invest = state.amount.toDoubleOrNull() ?: 0.0
            val win = state.winAmount.toDoubleOrNull() ?: 0.0
            if (win > 0 && win > invest) {
                MusicPlayer.playOnProfit(context)
            }
            onBack()
        }
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null) "添加记录" else "编辑记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (recordId != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // 投入金额
            OutlinedTextField(
                value = state.amount,
                onValueChange = vm::setAmount,
                label = { Text("投入金额（元）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 中奖金额
            OutlinedTextField(
                value = state.winAmount,
                onValueChange = vm::setWinAmount,
                label = { Text("中奖金额（元，未中留空）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 以下部分照抄彩票日记：标题 + 文字描述 + 附图
            DiaryInputs(
                title = state.title,
                content = state.note,
                imagePath = state.imagePath,
                onTitleChange = vm::setTitle,
                onContentChange = vm::setNote,
                onImagePathChange = vm::setImagePath
            )

            Spacer(Modifier.height(8.dp))

            // 保存按钮
            Button(
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (recordId == null) "保存记录" else "保存修改")
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm && recordId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("删除后数据将无法恢复，确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        vm.delete(recordId)
                        onBack()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
