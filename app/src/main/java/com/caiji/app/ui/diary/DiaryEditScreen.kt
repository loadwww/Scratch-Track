package com.caiji.app.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.ui.components.DiaryInputs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    vm: DiaryEditorViewModel,
    diaryId: Long?,
    onBack: () -> Unit
) {
    LaunchedEffect(diaryId) { vm.load(diaryId) }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (diaryId == null) "添加彩票日记" else "编辑彩票日记") },
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
            // 标题 + 文字描述 + 附图（三输入，有任一即可保存）
            DiaryInputs(
                title = state.title,
                content = state.content,
                imagePath = state.coverImagePath,
                onTitleChange = vm::setTitle,
                onContentChange = vm::setContent,
                onImagePathChange = vm::setCover
            )

            // 保存按钮
            Button(
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存日记")
            }
        }
    }
}
