package com.caiji.app.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caiji.app.data.backup.BackupManager
import com.caiji.app.data.prefs.BackgroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var marqueeText by remember(settings.marqueeTexts) {
        mutableStateOf(settings.marqueeTexts.joinToString("\n"))
    }
    var budgetText by remember(settings.monthlyBudgetDefault) {
        mutableStateOf(settings.monthlyBudgetDefault.toString())
    }
    var scratchCoinsText by remember(settings.scratchDailyCoins) {
        mutableStateOf(settings.scratchDailyCoins.toString())
    }
    var backupMessage by remember { mutableStateOf<String?>(null) }

    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = runCatching {
                    val path = copyUriToInternal(context, uri, "wallpaper.jpg")
                    vm.setWallpaper(path)
                    path
                }.isSuccess
                val msg = if (ok) "壁纸已设置" else "壁纸设置失败"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val musicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = runCatching {
                    val path = copyUriToInternal(context, uri, "custom_music.mp3")
                    vm.setMusicPath(path)
                    path
                }.isSuccess
                val msg = if (ok) "音乐文件已设置" else "音乐文件设置失败"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            // 首页轮换文字
            Section("首页轮换文字") {
                Text(
                    "每行输入一句提醒文字，首页点击文字条时将随机切换显示其中一句。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = marqueeText,
                    onValueChange = { marqueeText = it },
                    placeholder = { Text("例如：见好就收\n不要上头\n适可而止") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                Button(
                    onClick = {
                        val lines = marqueeText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        vm.setMarqueeTexts(lines.ifEmpty { listOf("见好就收") })
                        Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存轮换文字") }
            }

            // 盈利音乐
            Section("盈利庆祝音乐") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("盈利时播放音乐", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = settings.musicEnabled,
                        onCheckedChange = { vm.setMusicEnabled(it) }
                    )
                }
                Text(
                    "保存记录时如果盈利（中奖 > 投入），将自动播放庆祝音乐。默认使用内置《好运来》，也可选择自定义音频文件。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { musicLauncher.launch("audio/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.MusicNote, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("选择音乐")
                    }
                    if (settings.musicPath.isNotBlank()) {
                        OutlinedButton(
                            onClick = { vm.clearMusicPath() },
                            modifier = Modifier.weight(1f)
                        ) { Text("恢复默认") }
                    }
                }
                if (settings.musicPath.isNotBlank()) {
                    Text(
                        "当前音乐：${File(settings.musicPath).name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 首页壁纸背景图
            Section("首页壁纸背景") {
                Text(
                    "选择一张图片作为首页背景；设置后将覆盖纯色背景。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { wallpaperLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Wallpaper, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("选择壁纸")
                    }
                    if (settings.wallpaperPath.isNotBlank()) {
                        OutlinedButton(
                            onClick = { vm.clearWallpaper() },
                            modifier = Modifier.weight(1f)
                        ) { Text("清除壁纸") }
                    }
                }
                if (settings.wallpaperPath.isNotBlank()) {
                    Text(
                        "当前壁纸：${File(settings.wallpaperPath).name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 首页纯色背景
            Section("首页纯色背景") {
                BackgroundTheme.entries.forEach { theme ->
                    SelectableRow(
                        title = theme.title,
                        selected = settings.background == theme,
                        onClick = { vm.setBackground(theme) }
                    )
                }
            }

            // 月度预算默认值
            Section("月度购彩预算") {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("每月预算（元）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val amount = budgetText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) vm.setMonthlyBudgetDefault(amount)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存预算") }
            }

            // 预算提醒文字
            Section("预算提醒文字") {
                var nearAlertText by remember(settings.budgetNearAlert) { mutableStateOf(settings.budgetNearAlert) }
                var exceedAlertText by remember(settings.budgetExceedAlert) { mutableStateOf(settings.budgetExceedAlert) }
                OutlinedTextField(
                    value = nearAlertText,
                    onValueChange = { nearAlertText = it },
                    label = { Text("接近上限提醒（≥80%）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { vm.setBudgetNearAlert(nearAlertText.trim().ifEmpty { "预算已使用超过80%，请注意控制！" }) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存接近提醒") }
                OutlinedTextField(
                    value = exceedAlertText,
                    onValueChange = { exceedAlertText = it },
                    label = { Text("超支提醒（≥100%）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { vm.setBudgetExceedAlert(exceedAlertText.trim().ifEmpty { "本月预算已超支，请谨慎投注！" }) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存超支提醒") }
            }

            // 刮刮乐每日积分
            Section("刮刮乐每日积分") {
                OutlinedTextField(
                    value = scratchCoinsText,
                    onValueChange = { scratchCoinsText = it.filter { c -> c.isDigit() } },
                    label = { Text("每日领取金币数") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val coins = scratchCoinsText.toIntOrNull() ?: 0
                        if (coins > 0) vm.setScratchDailyCoins(coins)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存积分设置") }
            }

            // 数据备份
            Section("数据备份") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val path = BackupManager.exportToLocal(context)
                                backupMessage = "已导出到：$path"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("本地导出") }
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val ok = BackupManager.exportToCloud(context)
                                backupMessage = if (ok) "云盘导出已发起，请在系统对话框中选择保存位置"
                                else "云盘导出失败：未找到可用目录"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("云盘导出") }
                }
                backupMessage?.let { msg ->
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private suspend fun copyUriToInternal(context: Context, uri: Uri, fileName: String): String =
    withContext(Dispatchers.IO) {
        val outFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("无法读取所选文件")
        outFile.absolutePath
    }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun SelectableRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}
